package dev.toaru.blackholeultimate.mechanics;

import dev.toaru.blackholeultimate.BlackHoleUltimateMod;
import dev.toaru.blackholeultimate.Cfg;
import dev.toaru.blackholeultimate.net.FxType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import io.github.pinchan4273.reacademycraft.api.AbilityId;

/** Classic Black Hole (Level 3) - Balanced, reliable, the standard singularity. */
public final class ClassicBlackHole extends BlackHoleBase {
    private ClassicBlackHole() { }
    public static final ClassicBlackHole INSTANCE = new ClassicBlackHole();

    @Override protected DoubleValuePair getCost() { return new DoubleValuePair(Cfg.CLASSIC_CP, Cfg.CLASSIC_OVERLOAD); }
    @Override protected DoubleValuePair getCooldown() { return new DoubleValuePair(Cfg.CLASSIC_COOLDOWN, Cfg.CLASSIC_COOLDOWN); }
    @Override protected DoubleValue getFormationSeconds() { return Cfg.CLASSIC_FORMATION; }
    @Override protected DoubleValue getActiveSeconds() { return Cfg.CLASSIC_ACTIVE; }
    @Override protected DoubleValue getPullRadius() { return Cfg.CLASSIC_RADIUS; }
    @Override protected DoubleValue getHorizonRadius() { return Cfg.CLASSIC_HORIZON; }
    @Override protected DoubleValue getPullStrength() { return Cfg.CLASSIC_PULL; }
    @Override protected DoubleValue getMaxSpeed() { return Cfg.MAX_SPEED; }
    @Override protected DoubleValue getSwirl() { return Cfg.SWIRL; }
    @Override protected DoubleValue getHorizonDamage() { return Cfg.CLASSIC_HORIZON_DMG; }
    @Override protected DoubleValue getBlastRadius() { return Cfg.CLASSIC_BLAST_RADIUS; }
    @Override protected DoubleValue getBlastDamage() { return Cfg.CLASSIC_BLAST_DMG; }
    @Override protected DoubleValue getBlastKnockback() { return Cfg.CLASSIC_BLAST_KB; }
    @Override protected FxType getFxType() { return FxType.CLASSIC_BLACK_HOLE; }
    @Override protected FxType getCollapseFxType() { return FxType.CLASSIC_BLACK_HOLE_COLLAPSE; }
    @Override protected SoundEventPair getStartSound() { return new SoundEventPair(SoundEvents.EVOKER_PREPARE_ATTACK, 1.5f, 0.8f); }
    @Override protected SoundEventPair getAmbientSound() { return new SoundEventPair(SoundEvents.BEACON_AMBIENT, 1.0f, 0.9f); }
    @Override protected SoundEventPair getCollapseSound() { return new SoundEventPair(SoundEvents.GENERIC_EXPLODE, 2.0f, 0.6f); }

    @Override protected Vec3 getCentre(net.minecraft.server.level.ServerPlayer p) {
        return Targeting.point(p, 40).add(0, 4.0, 0);
    }

    @Override protected AbilityId getAbilityId() { return BlackHoleUltimateMod.BLACK_HOLE_CLASSIC; }

    public static void forget(java.util.UUID player) { BlackHoleBase.forget(player); }
    public static void clearAll() { BlackHoleBase.clearAll(); }
}