package dev.toaru.blackholeultimate.mechanics;

import dev.toaru.blackholeultimate.BlackHoleUltimateMod;
import dev.toaru.blackholeultimate.Cfg;
import dev.toaru.blackholeultimate.net.FxType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import io.github.pinchan4273.reacademycraft.api.AbilityId;

/** Void Black Hole (Level 5 - Ultimate) - True damage, massive radius, devouring everything. */
public final class VoidBlackHole extends BlackHoleBase {
    private VoidBlackHole() { }
    public static final VoidBlackHole INSTANCE = new VoidBlackHole();

    @Override protected DoubleValuePair getCost() { return new DoubleValuePair(Cfg.VOID_CP, Cfg.VOID_OVERLOAD); }
    @Override protected DoubleValuePair getCooldown() { return new DoubleValuePair(Cfg.VOID_COOLDOWN, Cfg.VOID_COOLDOWN); }
    @Override protected DoubleValue getFormationSeconds() { return Cfg.VOID_FORMATION; }
    @Override protected DoubleValue getActiveSeconds() { return Cfg.VOID_ACTIVE; }
    @Override protected DoubleValue getPullRadius() { return Cfg.VOID_RADIUS; }
    @Override protected DoubleValue getHorizonRadius() { return Cfg.VOID_HORIZON; }
    @Override protected DoubleValue getPullStrength() { return Cfg.VOID_PULL; }
    @Override protected DoubleValue getMaxSpeed() { return Cfg.MAX_SPEED; }
    @Override protected DoubleValue getSwirl() { return Cfg.SWIRL; }
    @Override protected DoubleValue getHorizonDamage() { return Cfg.VOID_HORIZON_DMG; }
    @Override protected DoubleValue getBlastRadius() { return Cfg.VOID_BLAST_RADIUS; }
    @Override protected DoubleValue getBlastDamage() { return Cfg.VOID_BLAST_DMG; }
    @Override protected DoubleValue getBlastKnockback() { return Cfg.VOID_BLAST_KB; }
    @Override protected FxType getFxType() { return FxType.VOID_BLACK_HOLE; }
    @Override protected FxType getCollapseFxType() { return FxType.VOID_BLACK_HOLE_COLLAPSE; }
    @Override protected SoundEventPair getStartSound() { return new SoundEventPair(SoundEvents.WITHER_SPAWN, 3.0f, 0.5f); }
    @Override protected SoundEventPair getAmbientSound() { return new SoundEventPair(SoundEvents.WITHER_AMBIENT, 2.0f, 0.4f); }
    @Override protected SoundEventPair getCollapseSound() { return new SoundEventPair(SoundEvents.WITHER_DEATH, 4.0f, 0.3f); }

    @Override protected Vec3 getCentre(net.minecraft.server.level.ServerPlayer p) {
        return Targeting.point(p, 40).add(0, 4.0, 0);
    }

    @Override protected AbilityId getAbilityId() { return BlackHoleUltimateMod.BLACK_HOLE_VOID; }

    @Override
    protected void applyEntityEffects(net.minecraft.server.level.ServerLevel level, Vec3 centre, double pullR, double horizon,
                                      double ramp, int activeAge, var actor, State s, ActivationContext c) {
        super.applyEntityEffects(level, centre, pullR, horizon, ramp, activeAge, actor, s, c);

        // Void tick: true damage (bypasses armor) to everything inside horizon
        if (activeAge % 10 == 0) {
            double voidDmg = Cfg.VOID_VOID_TICK_DMG.get();
            for (UUID id : s.affectedEntities) {
                Entity found = level.getEntity(id);
                if (!(found instanceof LivingEntity le) || !le.isAlive()) continue;
                Vec3 to = centre.subtract(Targeting.centre(le));
                if (to.length() <= horizon + le.getBbWidth() * 0.5) {
                    // True damage - hurt directly
                    le.hurt(level.damageSources().magic(), (float) voidDmg);
                    c.addProficiency(0.005f);
                }
            }
        }
    }

    public static void forget(java.util.UUID player) { BlackHoleBase.forget(player); }
    public static void clearAll() { BlackHoleBase.clearAll(); }
}