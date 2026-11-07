package dev.toaru.blackholeultimate.mechanics;

import dev.toaru.blackholeultimate.Cfg;
import dev.toaru.blackholeultimate.net.Fx;
import dev.toaru.blackholeultimate.net.FxType;
import io.github.pinchan4273.reacademycraft.api.ActivationOutcome;
import io.github.pinchan4273.reacademycraft.api.StopReason;
import io.github.pinchan4273.reacademycraft.api.mc.AcademySafety;
import io.github.pinchan4273.reacademycraft.api.mc.ActivationContext;
import io.github.pinchan4273.reacademycraft.api.mc.AbilityActor;
import io.github.pinchan4273.reacademycraft.api.mc.SkillBehavior;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

/**
 * Base class for all black hole abilities. Handles common state, targeting, visuals, and lifecycle.
 */
public abstract class BlackHoleBase implements SkillBehavior {
    protected static final class State {
        Vec3 centre;
        long fxKey;
        final Set<UUID> affectedEntities = new HashSet<>();
        final Set<UUID> affectedProjectiles = new HashSet<>();
        final Map<UUID, Vec3> originalMotions = new HashMap<>();
        int lastValidationTick = -1;
    }

    private static final Map<UUID, State> STATES = new HashMap<>();

    protected abstract DoubleValuePair getCost();
    protected abstract DoubleValuePair getCooldown();
    protected abstract DoubleValue getFormationSeconds();
    protected abstract DoubleValue getActiveSeconds();
    protected abstract DoubleValue getPullRadius();
    protected abstract DoubleValue getHorizonRadius();
    protected abstract DoubleValue getPullStrength();
    protected abstract DoubleValue getMaxSpeed();
    protected abstract DoubleValue getSwirl();
    protected abstract DoubleValue getHorizonDamage();
    protected abstract DoubleValue getBlastRadius();
    protected abstract DoubleValue getBlastDamage();
    protected abstract DoubleValue getBlastKnockback();
    protected abstract FxType getFxType();
    protected abstract FxType getCollapseFxType();
    protected abstract SoundEventPair getStartSound();
    protected abstract SoundEventPair getAmbientSound();
    protected abstract SoundEventPair getCollapseSound();

    protected record DoubleValuePair(DoubleValue cp, DoubleValue overload) {}
    protected record SoundEventPair(net.minecraft.sounds.SoundEvent event, float volume, float pitch) {}

    protected int formationTicks() { return Cfg.ticks(getFormationSeconds()); }
    protected int activeTicks() { return Cfg.ticks(getActiveSeconds()); }
    protected int totalTicks() { return formationTicks() + activeTicks(); }

    @Override
    public ActivationOutcome start(ActivationContext c) {
        ServerPlayer p = c.naturalUser();
        if (p == null) return ActivationOutcome.REFUSED_INVALID;

        Vec3 centre = getCentre(p);
        DoubleValuePair cost = getCost();
        if (!c.pay(cost.cp().get().floatValue(), cost.overload().get().floatValue())) {
            return ActivationOutcome.REFUSED_RESOURCE;
        }

        ServerLevel level = p.serverLevel();
        State s = new State();
        s.centre = centre;
        s.fxKey = Fx.make(level, centre, getFxType(), totalTicks()).range(220.0)
                .aux(new Vec3(getPullRadius().get(), formationTicks(), 0)).send();
        STATES.put(p.getUUID(), s);

        SoundEventPair sound = getStartSound();
        level.playSound(null, centre.x, centre.y, centre.z, sound.event(), sound.volume(), sound.pitch());
        return ActivationOutcome.STARTED;
    }

    @Override
    public boolean tick(ActivationContext c) {
        ServerPlayer p = c.naturalUser();
        State s = p == null ? null : STATES.get(p.getUUID());
        int age = c.age();
        if (p == null || s == null || age >= totalTicks()) return false;

        ServerLevel level = p.serverLevel();
        AbilityActor actor = c.actor();
        Vec3 centre = s.centre;
        double pullR = getPullRadius().get();
        double horizon = getHorizonRadius().get();
        double slowFactor = getPullStrength().get();
        int formTicks = formationTicks();
        double ramp = Math.min(1.0, age / (double) Math.max(1, formTicks));

        // Formation phase
        if (age < formTicks) {
            if (age % 10 == 0) playAmbientSound(level, centre, 0.5f);
            return true;
        }

        // Active phase
        int activeAge = age - formTicks;

        // Validate targets periodically
        if (activeAge % 4 == 0 && activeAge != s.lastValidationTick) {
            s.lastValidationTick = activeAge;
            validateTargets(level, centre, pullR, p, actor, s);
        }

        // Apply effects to entities
        applyEntityEffects(level, centre, pullR, horizon, ramp, activeAge, actor, s, c);

        // Apply effects to projectiles
        applyProjectileEffects(level, centre, pullR, horizon, ramp, s);

        // Periodic ambient sound
        if (activeAge % 20 == 0) playAmbientSound(level, centre, 0.7f);

        return true;
    }

    protected void validateTargets(ServerLevel level, Vec3 centre, double pullR, ServerPlayer p,
                                   AbilityActor actor, State s) {
        s.affectedEntities.clear();
        s.affectedProjectiles.clear();

        for (LivingEntity e : Targeting.livingWithin(level, centre, pullR * 1.05, p)) {
            if (Targeting.hostile(actor, e, getAbilityId()) && !s.originalMotions.containsKey(e.getUUID())) {
                s.affectedEntities.add(e.getUUID());
                s.originalMotions.put(e.getUUID(), e.getDeltaMovement());
            }
        }

        for (Projectile pr : Targeting.projectilesWithin(level, centre, pullR, p)) {
            if (!s.originalMotions.containsKey(pr.getUUID())) {
                s.affectedProjectiles.add(pr.getUUID());
                s.originalMotions.put(pr.getUUID(), pr.getDeltaMovement());
            }
        }
    }

    protected void applyEntityEffects(ServerLevel level, Vec3 centre, double pullR, double horizon,
                                      double ramp, int activeAge, AbilityActor actor, State s, ActivationContext c) {
        double pullStr = getPullStrength().get();
        double swirlStr = Cfg.SWIRL.get();
        double maxSpd = getMaxSpeed().get();
        double bossPull = Cfg.BOSS_PULL.get();
        double bossDmg = Cfg.BOSS_DMG.get();
        double hzDmg = getHorizonDamage().get();

        int hits = 0;
        for (UUID id : s.affectedEntities) {
            Entity found = level.getEntity(id);
            if (!(found instanceof LivingEntity le) || !le.isAlive()) continue;
            Vec3 to = centre.subtract(Targeting.centre(le));
            double d = to.length();
            if (d < 0.01 || d > pullR * 1.3) continue;

            Vec3 dir = to.scale(1.0 / d);
            double strength = Targeting.bossLike(le) ? bossPull : Targeting.controlFactor(le);
            double closeness = Math.max(0.0, 1.0 - d / pullR);
            double accel = pullStr * ramp * strength * (0.25 + 0.75 * Math.pow(closeness, 1.5));
            Vec3 swirl = new Vec3(-dir.z, 0, dir.x).scale(swirlStr * ramp * strength * closeness);
            Vec3 v = le.getDeltaMovement().scale(0.96).add(dir.scale(accel)).add(swirl);
            Targeting.setMotion(le, Targeting.capLength(v, maxSpd));

            // Horizon damage
            boolean pulse = activeAge % 5 == 0 && ramp >= 0.6;
            if (pulse && d <= horizon + le.getBbWidth() * 0.5) {
                double dmg = hzDmg * (Targeting.bossLike(le) ? bossDmg : 1.0);
                if (AcademySafety.attack(actor, le, getAbilityId(), (float) dmg)) hits++;
            }
        }

        if (hits > 0) c.addProficiency(0.003f * Math.min(hits, 4));
    }

    protected void applyProjectileEffects(ServerLevel level, Vec3 centre, double pullR, double horizon,
                                          double ramp, State s) {
        for (UUID id : s.affectedProjectiles) {
            Entity found = level.getEntity(id);
            if (!(found instanceof Projectile pr) || !pr.isAlive()) continue;
            Vec3 to = centre.subtract(pr.position());
            double d = to.length();
            if (d <= horizon) { pr.discard(); continue; }
            Vec3 v = pr.getDeltaMovement().scale(0.9).add(to.scale(1.0 / d).scale(0.5 * ramp));
            Targeting.setMotion(pr, Targeting.capLength(v, 2.5));
        }
    }

    @Override
    public void stop(ActivationContext c, StopReason reason) {
        ServerPlayer p = c.naturalUser();
        State s = p == null ? null : STATES.remove(p.getUUID());
        if (p != null && s != null) {
            ServerLevel level = p.serverLevel();
            Fx.cancel(level, s.fxKey);

            // Restore motion
            for (Map.Entry<UUID, Vec3> entry : s.originalMotions.entrySet()) {
                Entity e = level.getEntity(entry.getKey());
                if (e != null && e.isAlive()) {
                    Targeting.setMotion(e, entry.getValue());
                }
            }

            if (reason == StopReason.FINISHED || reason == StopReason.SWITCHED_OFF) {
                collapse(c, p, s, level);
            }
        }
        DoubleValuePair cd = getCooldown();
        c.setCooldown(Cfg.ticks(cd.cp())); // cooldown uses cp field for seconds
    }

    protected void collapse(ActivationContext c, ServerPlayer p, State s, ServerLevel level) {
        AbilityActor actor = c.actor();
        double radius = getBlastRadius().get();
        double dmg = getBlastDamage().get();
        double kb = getBlastKnockback().get();
        double bossDmg = Cfg.BOSS_DMG.get();
        double bossKb = Cfg.BOSS_KB.get();

        int hits = 0;
        for (LivingEntity e : Targeting.livingWithin(level, s.centre, radius, p)) {
            if (!Targeting.hostile(actor, e, getAbilityId())) continue;
            double d = Targeting.centre(e).distanceTo(s.centre);
            double falloff = 1.0 - 0.7 * Math.min(1.0, d / radius);
            double bossMul = Targeting.bossLike(e) ? bossDmg : 1.0;
            if (!AcademySafety.attack(actor, e, getAbilityId(), (float) (dmg * falloff * bossMul))) continue;
            Vec3 away = Targeting.centre(e).subtract(s.centre);
            away = away.lengthSqr() < 1e-4 ? new Vec3(0, 1, 0) : away.normalize();
            double k = kb * falloff * (Targeting.bossLike(e) ? bossKb : Targeting.controlFactor(e));
            Targeting.setMotion(e, new Vec3(away.x * k, 0.3 + away.y * k * 0.6, away.z * k));
            hits++;
        }

        // Collapse visual
        Fx.make(level, s.centre, getCollapseFxType(), 26).range(220.0)
                .aux(new Vec3(radius, getHorizonRadius().get(), 0)).send();

        SoundEventPair sound = getCollapseSound();
        level.playSound(null, s.centre.x, s.centre.y, s.centre.z, sound.event(), sound.volume(), sound.pitch());

        if (hits > 0) c.addProficiency(0.01f * Math.min(hits, 5));
    }

    protected void playAmbientSound(ServerLevel level, Vec3 pos, float volume) {
        SoundEventPair sound = getAmbientSound();
        level.playSound(null, pos.x, pos.y, pos.z, sound.event(), volume, sound.pitch());
    }

    protected abstract Vec3 getCentre(ServerPlayer p);
    protected abstract AbilityId getAbilityId();

    public static void forget(UUID player) { STATES.remove(player); }
    public static void clearAll() { STATES.clear(); }
}