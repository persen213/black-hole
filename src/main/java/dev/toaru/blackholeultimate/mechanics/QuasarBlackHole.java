package dev.toaru.blackholeultimate.mechanics;

import dev.toaru.blackholeultimate.BlackHoleUltimateMod;
import dev.toaru.blackholeultimate.Cfg;
import dev.toaru.blackholeultimate.net.FxType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import io.github.pinchan4273.reacademycraft.api.AbilityId;

/** Quasar Black Hole (Level 4) - Emits a damaging beam while pulling. Hybrid offense. */
public final class QuasarBlackHole extends BlackHoleBase {
    private QuasarBlackHole() { }
    public static final QuasarBlackHole INSTANCE = new QuasarBlackHole();

    @Override protected DoubleValuePair getCost() { return new DoubleValuePair(Cfg.QUASAR_CP, Cfg.QUASAR_OVERLOAD); }
    @Override protected DoubleValuePair getCooldown() { return new DoubleValuePair(Cfg.QUASAR_COOLDOWN, Cfg.QUASAR_COOLDOWN); }
    @Override protected DoubleValue getFormationSeconds() { return Cfg.QUASAR_FORMATION; }
    @Override protected DoubleValue getActiveSeconds() { return Cfg.QUASAR_ACTIVE; }
    @Override protected DoubleValue getPullRadius() { return Cfg.QUASAR_RADIUS; }
    @Override protected DoubleValue getHorizonRadius() { return Cfg.QUASAR_HORIZON; }
    @Override protected DoubleValue getPullStrength() { return Cfg.QUASAR_PULL; }
    @Override protected DoubleValue getMaxSpeed() { return Cfg.MAX_SPEED; }
    @Override protected DoubleValue getSwirl() { return Cfg.SWIRL; }
    @Override protected DoubleValue getHorizonDamage() { return Cfg.QUASAR_HORIZON_DMG; }
    @Override protected DoubleValue getBlastRadius() { return Cfg.QUASAR_BLAST_RADIUS; }
    @Override protected DoubleValue getBlastDamage() { return Cfg.QUASAR_BLAST_DMG; }
    @Override protected DoubleValue getBlastKnockback() { return Cfg.QUASAR_BLAST_KB; }
    @Override protected FxType getFxType() { return FxType.QUASAR_BLACK_HOLE; }
    @Override protected FxType getCollapseFxType() { return FxType.QUASAR_BLACK_HOLE_COLLAPSE; }
    @Override protected SoundEventPair getStartSound() { return new SoundEventPair(SoundEvents.WARDEN_SONIC_BOOM, 2.0f, 0.7f); }
    @Override protected SoundEventPair getAmbientSound() { return new SoundEventPair(SoundEvents.BEACON_AMBIENT, 1.5f, 0.6f); }
    @Override protected SoundEventPair getCollapseSound() { return new SoundEventPair(SoundEvents.WARDEN_SONIC_CHARGE, 3.0f, 0.5f); }

    @Override protected Vec3 getCentre(net.minecraft.server.level.ServerPlayer p) {
        return Targeting.point(p, 40).add(0, 4.0, 0);
    }

    @Override protected AbilityId getAbilityId() { return BlackHoleUltimateMod.BLACK_HOLE_QUASAR; }

    @Override
    protected void applyEntityEffects(net.minecraft.server.level.ServerLevel level, Vec3 centre, double pullR, double horizon,
                                      double ramp, int activeAge, var actor, State s, ActivationContext c) {
        super.applyEntityEffects(level, centre, pullR, horizon, ramp, activeAge, actor, s, c);

        // Quasar beam: damages in a line from centre to furthest affected entity
        if (activeAge % 10 == 0 && !s.affectedEntities.isEmpty()) {
            double beamDmg = Cfg.QUASAR_BEAM_DMG.get();
            double beamRange = Cfg.QUASAR_BEAM_RANGE.get();
            for (UUID id : s.affectedEntities) {
                Entity found = level.getEntity(id);
                if (!(found instanceof LivingEntity le) || !le.isAlive()) continue;
                Vec3 to = Targeting.centre(le).subtract(centre);
                double d = to.length();
                if (d > beamRange) continue;
                var safety = io.github.pinchan4273.reacademycraft.api.mc.AcademySafety.attack(actor, le, getAbilityId(), (float) beamDmg);
                if (safety) c.addProficiency(0.002f);
            }
        }
    }

    public static void forget(java.util.UUID player) { BlackHoleBase.forget(player); }
    public static void clearAll() { BlackHoleBase.clearAll(); }
}