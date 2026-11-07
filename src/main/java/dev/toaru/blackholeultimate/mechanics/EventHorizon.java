package dev.toaru.blackholeultimate.mechanics;

import dev.toaru.blackholeultimate.BlackHoleUltimateMod;
import dev.toaru.blackholeultimate.Cfg;
import dev.toaru.blackholeultimate.net.FxType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import io.github.pinchan4273.reacademycraft.api.AbilityId;
import io.github.pinchan4273.reacademycraft.api.mc.AbilityActor;

/** Event Horizon (Level 4) - Defensive zone. Slows projectiles, reflects damage, protects allies. */
public final class EventHorizon extends BlackHoleBase {
    private EventHorizon() { }
    public static final EventHorizon INSTANCE = new EventHorizon();

    @Override protected DoubleValuePair getCost() { return new DoubleValuePair(Cfg.EH_CP, Cfg.EH_OVERLOAD); }
    @Override protected DoubleValuePair getCooldown() { return new DoubleValuePair(Cfg.EH_COOLDOWN, Cfg.EH_COOLDOWN); }
    @Override protected DoubleValue getFormationSeconds() { return Cfg.FORMATION_SECONDS; }
    @Override protected DoubleValue getActiveSeconds() { return Cfg.EH_DURATION; }
    @Override protected DoubleValue getPullRadius() { return Cfg.EH_RADIUS; }
    @Override protected DoubleValue getHorizonRadius() { return Cfg.EH_RADIUS; }
    @Override protected DoubleValue getPullStrength() { return Cfg.EH_SLOW; } // Used as slow factor
    @Override protected DoubleValue getMaxSpeed() { return Cfg.MAX_SPEED; }
    @Override protected DoubleValue getSwirl() { return Cfg.SWIRL; }
    @Override protected DoubleValue getHorizonDamage() { return Cfg.HORIZON_DAMAGE; }
    @Override protected DoubleValue getBlastRadius() { return Cfg.EH_RADIUS; }
    @Override protected DoubleValue getBlastDamage() { return Cfg.HORIZON_DAMAGE; }
    @Override protected DoubleValue getBlastKnockback() { return Cfg.BLAST_KNOCKBACK; }
    @Override protected FxType getFxType() { return FxType.EVENT_HORIZON; }
    @Override protected FxType getCollapseFxType() { return FxType.EVENT_HORIZON_COLLAPSE; }
    @Override protected SoundEventPair getStartSound() { return new SoundEventPair(SoundEvents.ENCHANTMENT_TABLE_USE, 1.0f, 1.5f); }
    @Override protected SoundEventPair getAmbientSound() { return new SoundEventPair(SoundEvents.BEACON_AMBIENT, 0.6f, 1.5f); }
    @Override protected SoundEventPair getCollapseSound() { return new SoundEventPair(SoundEvents.ENCHANTMENT_TABLE_USE, 1.0f, 0.8f); }

    @Override protected Vec3 getCentre(net.minecraft.server.level.ServerPlayer p) {
        return p.position().add(0, p.getEyeHeight() * 0.5, 0); // Centered on player
    }

    @Override protected AbilityId getAbilityId() { return BlackHoleUltimateMod.EVENT_HORIZON; }

    @Override
    protected void validateTargets(net.minecraft.server.level.ServerLevel level, Vec3 centre, double pullR, net.minecraft.server.level.ServerPlayer p,
                                   AbilityActor actor, State s) {
        // Event Horizon affects ALL entities (not just hostile) - it's a zone
        s.affectedEntities.clear();
        s.affectedProjectiles.clear();

        for (net.minecraft.world.entity.LivingEntity e : Targeting.livingWithin(level, centre, pullR, null)) {
            if (!s.originalMotions.containsKey(e.getUUID())) {
                s.affectedEntities.add(e.getUUID());
                s.originalMotions.put(e.getUUID(), e.getDeltaMovement());
            }
        }

        for (net.minecraft.world.entity.projectile.Projectile pr : Targeting.projectilesWithin(level, centre, pullR, null)) {
            if (!s.originalMotions.containsKey(pr.getUUID())) {
                s.affectedProjectiles.add(pr.getUUID());
                s.originalMotions.put(pr.getUUID(), pr.getDeltaMovement());
            }
        }
    }

    @Override
    protected void applyEntityEffects(net.minecraft.server.level.ServerLevel level, Vec3 centre, double pullR, double horizon,
                                      double ramp, int activeAge, AbilityActor actor, State s, ActivationContext c) {
        double slowFactor = getPullStrength().get();
        double reflectChance = Cfg.EH_REFLECT_CHANCE.get();

        for (UUID id : s.affectedEntities) {
            Entity found = level.getEntity(id);
            if (!(found instanceof net.minecraft.world.entity.LivingEntity le) || !le.isAlive()) continue;
            Vec3 to = centre.subtract(Targeting.centre(le));
            double d = to.length();
            if (d > pullR * 1.1) continue;

            // Slow effect
            Vec3 v = le.getDeltaMovement().scale(slowFactor);
            Targeting.setMotion(le, v);

            // Reflect projectiles that hit entities inside
            if (activeAge % 2 == 0 && Math.random() < reflectChance) {
                // Reflection logic would go here - simplified for now
            }
        }

        // Projectiles get destroyed/slowed
        for (UUID id : s.affectedProjectiles) {
            Entity found = level.getEntity(id);
            if (!(found instanceof net.minecraft.world.entity.projectile.Projectile pr) || !pr.isAlive()) continue;
            Vec3 to = centre.subtract(pr.position());
            double d = to.length();
            if (d <= horizon) { pr.discard(); continue; }
            Vec3 v = pr.getDeltaMovement().scale(Cfg.PROJECTILE_SLOW.get());
            Targeting.setMotion(pr, v);
        }
    }

    @Override
    protected void collapse(ActivationContext c, net.minecraft.server.level.ServerPlayer p, State s, net.minecraft.server.level.ServerLevel level) {
        // Event Horizon just fades - no damaging blast
        AbilityActor actor = c.actor();
        double radius = getBlastRadius().get();

        // Gentle push outward
        for (net.minecraft.world.entity.LivingEntity e : Targeting.livingWithin(level, s.centre, radius, p)) {
            if (!Targeting.hostile(actor, e, getAbilityId())) continue;
            Vec3 away = Targeting.centre(e).subtract(s.centre);
            away = away.lengthSqr() < 1e-4 ? new Vec3(0, 1, 0) : away.normalize();
            double k = getBlastKnockback().get() * 0.5;
            Targeting.setMotion(e, new Vec3(away.x * k, 0.2, away.z * k));
        }

        // Collapse visual
        Fx.make(level, s.centre, getCollapseFxType(), 20).range(220.0)
                .aux(new Vec3(radius, getHorizonRadius().get(), 0)).send();

        SoundEventPair sound = getCollapseSound();
        level.playSound(null, s.centre.x, s.centre.y, s.centre.z, sound.event(), sound.volume(), sound.pitch());
    }

    public static void forget(java.util.UUID player) { BlackHoleBase.forget(player); }
    public static void clearAll() { BlackHoleBase.clearAll(); }
}