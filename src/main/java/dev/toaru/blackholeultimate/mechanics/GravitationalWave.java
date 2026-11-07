package dev.toaru.blackholeultimate.mechanics;

import dev.toaru.blackholeultimate.BlackHoleUltimateMod;
import dev.toaru.blackholeultimate.Cfg;
import dev.toaru.blackholeultimate.net.FxType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import io.github.pinchan4273.reacademycraft.api.AbilityId;

/** Gravitational Wave (Level 3) - Emits expanding rings of force damage and knockback. */
public final class GravitationalWave extends BlackHoleBase {
    private GravitationalWave() { }
    public static final GravitationalWave INSTANCE = new GravitationalWave();

    @Override protected DoubleValuePair getCost() { return new DoubleValuePair(Cfg.GW_CP, Cfg.GW_OVERLOAD); }
    @Override protected DoubleValuePair getCooldown() { return new DoubleValuePair(Cfg.GW_COOLDOWN, Cfg.GW_COOLDOWN); }
    @Override protected DoubleValue getFormationSeconds() { return Cfg.FORMATION_SECONDS; }
    @Override protected DoubleValue getActiveSeconds() { return Cfg.GW_DELAY; } // Uses wave delay as active
    @Override protected DoubleValue getPullRadius() { return Cfg.GW_RADIUS; }
    @Override protected DoubleValue getHorizonRadius() { return Cfg.HORIZON_RADIUS; }
    @Override protected DoubleValue getPullStrength() { return Cfg.PULL_STRENGTH; }
    @Override protected DoubleValue getMaxSpeed() { return Cfg.MAX_SPEED; }
    @Override protected DoubleValue getSwirl() { return Cfg.SWIRL; }
    @Override protected DoubleValue getHorizonDamage() { return Cfg.GW_DMG; }
    @Override protected DoubleValue getBlastRadius() { return Cfg.GW_RADIUS; }
    @Override protected DoubleValue getBlastDamage() { return Cfg.GW_DMG; }
    @Override protected DoubleValue getBlastKnockback() { return Cfg.GW_KB; }
    @Override protected FxType getFxType() { return FxType.GRAVITATIONAL_WAVE; }
    @Override protected FxType getCollapseFxType() { return FxType.GRAVITATIONAL_WAVE_COLLAPSE; }
    @Override protected SoundEventPair getStartSound() { return new SoundEventPair(SoundEvents.WARDEN_HEARTBEAT, 2.0f, 0.8f); }
    @Override protected SoundEventPair getAmbientSound() { return new SoundEventPair(SoundEvents.BEACON_AMBIENT, 1.0f, 1.0f); }
    @Override protected SoundEventPair getCollapseSound() { return new SoundEventPair(SoundEvents.WARDEN_SONIC_BOOM, 3.0f, 0.6f); }

    @Override protected Vec3 getCentre(net.minecraft.server.level.ServerPlayer p) {
        return p.position().add(0, p.getEyeHeight() * 0.5, 0);
    }

    @Override protected AbilityId getAbilityId() { return BlackHoleUltimateMod.GRAVITATIONAL_WAVE; }

    @Override
    protected void validateTargets(net.minecraft.server.level.ServerLevel level, Vec3 centre, double pullR, net.minecraft.server.level.ServerPlayer p,
                                   var actor, State s) {
        s.affectedEntities.clear();
        for (net.minecraft.world.entity.LivingEntity e : Targeting.livingWithin(level, centre, pullR, p)) {
            if (Targeting.hostile(actor, e, getAbilityId()) && !s.originalMotions.containsKey(e.getUUID())) {
                s.affectedEntities.add(e.getUUID());
                s.originalMotions.put(e.getUUID(), e.getDeltaMovement());
            }
        }
    }

    @Override
    protected void applyEntityEffects(net.minecraft.server.level.ServerLevel level, Vec3 centre, double pullR, double horizon,
                                      double ramp, int activeAge, var actor, State s, ActivationContext c) {
        // Gravitational Wave emits waves every few ticks
        int waveCount = Cfg.GW_WAVES.get().intValue();
        int waveDelay = Cfg.ticks(Cfg.GW_DELAY);
        int waveIndex = activeAge / waveDelay;

        if (waveIndex >= waveCount) return;

        // Emit wave at intervals
        if (activeAge % waveDelay == 0) {
            double dmg = Cfg.GW_DMG.get();
            double kb = Cfg.GW_KB.get();
            double bossDmg = Cfg.BOSS_DMG.get();
            double bossKb = Cfg.BOSS_KB.get();
            double waveRadius = pullR * (waveIndex + 1) / (double) waveCount;

            for (UUID id : s.affectedEntities) {
                Entity found = level.getEntity(id);
                if (!(found instanceof net.minecraft.world.entity.LivingEntity le) || !le.isAlive()) continue;
                double d = Targeting.centre(le).distanceTo(centre);
                if (d > waveRadius * 1.2) continue; // Only hit by current wave

                double falloff = 1.0 - 0.5 * (d / waveRadius);
                double bossMul = Targeting.bossLike(le) ? bossDmg : 1.0;
                if (!io.github.pinchan4273.reacademycraft.api.mc.AcademySafety.attack(actor, le, getAbilityId(), (float) (dmg * falloff * bossMul))) continue;

                Vec3 away = Targeting.centre(le).subtract(centre);
                away = away.lengthSqr() < 1e-4 ? new Vec3(0, 1, 0) : away.normalize();
                double k = kb * falloff * (Targeting.bossLike(le) ? bossKb : Targeting.controlFactor(le));
                Targeting.setMotion(le, new Vec3(away.x * k, 0.5 + away.y * k * 0.5, away.z * k));
                c.addProficiency(0.005f);
            }

            // Visual wave effect
            dev.toaru.blackholeultimate.net.Fx.make(level, centre, FxType.GRAVITATIONAL_WAVE_RIPPLE, 20).range(220.0)
                    .aux(new Vec3(waveRadius, 0, waveIndex)).send();
        }
    }

    @Override
    protected void collapse(ActivationContext c, net.minecraft.server.level.ServerPlayer p, State s, net.minecraft.server.level.ServerLevel level) {
        // Final massive wave
        var actor = c.actor();
        double radius = getBlastRadius().get();
        double dmg = getBlastDamage().get() * 1.5;
        double kb = getBlastKnockback().get() * 1.5;
        double bossDmg = Cfg.BOSS_DMG.get();
        double bossKb = Cfg.BOSS_KB.get();

        for (net.minecraft.world.entity.LivingEntity e : Targeting.livingWithin(level, s.centre, radius, p)) {
            if (!Targeting.hostile(actor, e, getAbilityId())) continue;
            double d = Targeting.centre(e).distanceTo(s.centre);
            double falloff = 1.0 - 0.3 * Math.min(1.0, d / radius);
            double bossMul = Targeting.bossLike(e) ? bossDmg : 1.0;
            if (!io.github.pinchan4273.reacademycraft.api.mc.AcademySafety.attack(actor, e, getAbilityId(), (float) (dmg * falloff * bossMul))) continue;
            Vec3 away = Targeting.centre(e).subtract(s.centre);
            away = away.lengthSqr() < 1e-4 ? new Vec3(0, 1, 0) : away.normalize();
            double k = kb * falloff * (Targeting.bossLike(e) ? bossKb : Targeting.controlFactor(e));
            Targeting.setMotion(e, new Vec3(away.x * k, 1.0 + away.y * k * 0.5, away.z * k));
        }

        Fx.make(level, s.centre, getCollapseFxType(), 30).range(220.0)
                .aux(new Vec3(radius, 0, 0)).send();

        SoundEventPair sound = getCollapseSound();
        level.playSound(null, s.centre.x, s.centre.y, s.centre.z, sound.event(), sound.volume(), sound.pitch());
    }

    public static void forget(java.util.UUID player) { BlackHoleBase.forget(player); }
    public static void clearAll() { BlackHoleBase.clearAll(); }
}