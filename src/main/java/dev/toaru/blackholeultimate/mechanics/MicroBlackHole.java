package dev.toaru.blackholeultimate.mechanics;

import dev.toaru.blackholeultimate.BlackHoleUltimateMod;
import dev.toaru.blackholeultimate.Cfg;
import dev.toaru.blackholeultimate.net.FxType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import io.github.pinchan4273.reacademycraft.api.AbilityId;

/** Micro Black Hole (Level 2) - Small, fast, utility. Great for quick crowd control. */
public final class MicroBlackHole extends BlackHoleBase {
    private MicroBlackHole() { }
    public static final MicroBlackHole INSTANCE = new MicroBlackHole();

    @Override protected DoubleValuePair getCost() { return new DoubleValuePair(Cfg.MICRO_CP, Cfg.MICRO_OVERLOAD); }
    @Override protected DoubleValuePair getCooldown() { return new DoubleValuePair(Cfg.MICRO_COOLDOWN, Cfg.MICRO_COOLDOWN); }
    @Override protected DoubleValue getFormationSeconds() { return Cfg.MICRO_FORMATION; }
    @Override protected DoubleValue getActiveSeconds() { return Cfg.MICRO_ACTIVE; }
    @Override protected DoubleValue getPullRadius() { return Cfg.MICRO_RADIUS; }
    @Override protected DoubleValue getHorizonRadius() { return Cfg.MICRO_HORIZON; }
    @Override protected DoubleValue getPullStrength() { return Cfg.MICRO_PULL; }
    @Override protected DoubleValue getMaxSpeed() { return Cfg.MAX_SPEED; }
    @Override protected DoubleValue getSwirl() { return Cfg.SWIRL; }
    @Override protected DoubleValue getHorizonDamage() { return Cfg.MICRO_HORIZON_DMG; }
    @Override protected DoubleValue getBlastRadius() { return Cfg.MICRO_BLAST_RADIUS; }
    @Override protected DoubleValue getBlastDamage() { return Cfg.MICRO_BLAST_DMG; }
    @Override protected DoubleValue getBlastKnockback() { return Cfg.MICRO_BLAST_KB; }
    @Override protected FxType getFxType() { return FxType.MICRO_BLACK_HOLE; }
    @Override protected FxType getCollapseFxType() { return FxType.MICRO_BLACK_HOLE_COLLAPSE; }
    @Override protected SoundEventPair getStartSound() { return new SoundEventPair(SoundEvents.EVOKER_PREPARE_ATTACK, 1.2f, 1.0f); }
    @Override protected SoundEventPair getAmbientSound() { return new SoundEventPair(SoundEvents.BEACON_AMBIENT, 0.8f, 1.2f); }
    @Override protected SoundEventPair getCollapseSound() { return new SoundEventPair(SoundEvents.EVOKER_CAST_SPELL, 1.5f, 0.8f); }

    @Override protected Vec3 getCentre(net.minecraft.server.level.ServerPlayer p) {
        return p.position().add(0, p.getEyeHeight() * 0.85, 0);
    }

    @Override protected AbilityId getAbilityId() { return BlackHoleUltimateMod.BLACK_HOLE_MICRO; }

    public static void forget(java.util.UUID player) { BlackHoleBase.forget(player); }
    public static void clearAll() { BlackHoleBase.clearAll(); }
}