package dev.toaru.blackholeultimate;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

/**
 * All balance numbers for every black hole variant. Created at config/blackholeultimate-common.toml on first launch.
 */
public final class Cfg {
    private Cfg() { }
    private static final ForgeConfigSpec.Builder B = new ForgeConfigSpec.Builder();
    private static DoubleValue d(String name, double def, double min, double max) { return B.defineInRange(name, def, min, max); }
    private static IntValue i(String name, int def, int min, int max) { return B.defineInRange(name, def, min, max); }

    // === Classic Black Hole (Level 3) ===
    public static final DoubleValue CLASSIC_CP = d("classic.cp_cost", 5000, 0, 100000);
    public static final DoubleValue CLASSIC_OVERLOAD = d("classic.overload_cost", 200, 0, 10000);
    public static final IntValue CLASSIC_COOLDOWN = i("classic.cooldown_seconds", 120, 0, 3600);
    public static final DoubleValue CLASSIC_RADIUS = d("classic.pull_radius", 20, 5, 64);
    public static final DoubleValue CLASSIC_HORIZON = d("classic.horizon_radius", 3.0, 1, 10);
    public static final DoubleValue CLASSIC_PULL = d("classic.pull_strength", 0.15, 0.01, 1);
    public static final DoubleValue CLASSIC_HORIZON_DMG = d("classic.horizon_damage", 8, 0, 500);
    public static final DoubleValue CLASSIC_BLAST_RADIUS = d("classic.blast_radius", 18, 3, 64);
    public static final DoubleValue CLASSIC_BLAST_DMG = d("classic.blast_damage", 15, 0, 500);
    public static final DoubleValue CLASSIC_BLAST_KB = d("classic.blast_knockback", 2.0, 0, 8);
    public static final DoubleValue CLASSIC_FORMATION = d("classic.formation_seconds", 1.0, 0.2, 5);
    public static final DoubleValue CLASSIC_ACTIVE = d("classic.active_seconds", 6, 1, 60);

    // === Micro Black Hole (Level 2) ===
    public static final DoubleValue MICRO_CP = d("micro.cp_cost", 1500, 0, 50000);
    public static final DoubleValue MICRO_OVERLOAD = d("micro.overload_cost", 80, 0, 5000);
    public static final IntValue MICRO_COOLDOWN = i("micro.cooldown_seconds", 30, 0, 1800);
    public static final DoubleValue MICRO_RADIUS = d("micro.pull_radius", 8, 2, 24);
    public static final DoubleValue MICRO_HORIZON = d("micro.horizon_radius", 1.5, 0.5, 5);
    public static final DoubleValue MICRO_PULL = d("micro.pull_strength", 0.12, 0.01, 0.5);
    public static final DoubleValue MICRO_HORIZON_DMG = d("micro.horizon_damage", 4, 0, 200);
    public static final DoubleValue MICRO_BLAST_RADIUS = d("micro.blast_radius", 10, 2, 32);
    public static final DoubleValue MICRO_BLAST_DMG = d("micro.blast_damage", 8, 0, 200);
    public static final DoubleValue MICRO_BLAST_KB = d("micro.blast_knockback", 1.2, 0, 4);
    public static final DoubleValue MICRO_FORMATION = d("micro.formation_seconds", 0.4, 0.1, 2);
    public static final DoubleValue MICRO_ACTIVE = d("micro.active_seconds", 3, 1, 30);

    // === Quasar Black Hole (Level 4) ===
    public static final DoubleValue QUASAR_CP = d("quasar.cp_cost", 8000, 0, 100000);
    public static final DoubleValue QUASAR_OVERLOAD = d("quasar.overload_cost", 300, 0, 10000);
    public static final IntValue QUASAR_COOLDOWN = i("quasar.cooldown_seconds", 180, 0, 3600);
    public static final DoubleValue QUASAR_RADIUS = d("quasar.pull_radius", 24, 5, 80);
    public static final DoubleValue QUASAR_HORIZON = d("quasar.horizon_radius", 4.0, 1, 12);
    public static final DoubleValue QUASAR_PULL = d("quasar.pull_strength", 0.18, 0.01, 1);
    public static final DoubleValue QUASAR_HORIZON_DMG = d("quasar.horizon_damage", 12, 0, 500);
    public static final DoubleValue QUASAR_BEAM_DMG = d("quasar.beam_damage", 25, 0, 1000);
    public static final DoubleValue QUASAR_BEAM_RANGE = d("quasar.beam_range", 30, 5, 100);
    public static final DoubleValue QUASAR_BLAST_RADIUS = d("quasar.blast_radius", 22, 5, 80);
    public static final DoubleValue QUASAR_BLAST_DMG = d("quasar.blast_damage", 20, 0, 500);
    public static final DoubleValue QUASAR_BLAST_KB = d("quasar.blast_knockback", 2.5, 0, 10);
    public static final DoubleValue QUASAR_FORMATION = d("quasar.formation_seconds", 1.5, 0.2, 5);
    public static final DoubleValue QUASAR_ACTIVE = d("quasar.active_seconds", 8, 1, 60);

    // === Void Black Hole (Level 5 - Ultimate) ===
    public static final DoubleValue VOID_CP = d("void.cp_cost", 12000, 0, 200000);
    public static final DoubleValue VOID_OVERLOAD = d("void.overload_cost", 500, 0, 20000);
    public static final IntValue VOID_COOLDOWN = i("void.cooldown_seconds", 300, 0, 7200);
    public static final DoubleValue VOID_RADIUS = d("void.pull_radius", 32, 10, 128);
    public static final DoubleValue VOID_HORIZON = d("void.horizon_radius", 5.0, 2, 20);
    public static final DoubleValue VOID_PULL = d("void.pull_strength", 0.22, 0.01, 1);
    public static final DoubleValue VOID_HORIZON_DMG = d("void.horizon_damage", 20, 0, 1000);
    public static final DoubleValue VOID_BLAST_RADIUS = d("void.blast_radius", 30, 10, 128);
    public static final DoubleValue VOID_BLAST_DMG = d("void.blast_damage", 35, 0, 1000);
    public static final DoubleValue VOID_BLAST_KB = d("void.blast_knockback", 3.5, 0, 15);
    public static final DoubleValue VOID_FORMATION = d("void.formation_seconds", 2.0, 0.5, 10);
    public static final DoubleValue VOID_ACTIVE = d("void.active_seconds", 10, 1, 120);
    public static final DoubleValue VOID_VOID_TICK_DMG = d("void.void_tick_damage", 6, 0, 500); // True damage inside horizon

    // === Event Horizon (Level 4 - Defensive) ===
    public static final DoubleValue EH_CP = d("event_horizon.cp_cost", 6000, 0, 100000);
    public static final DoubleValue EH_OVERLOAD = d("event_horizon.overload_cost", 200, 0, 10000);
    public static final IntValue EH_COOLDOWN = i("event_horizon.cooldown_seconds", 90, 0, 3600);
    public static final DoubleValue EH_RADIUS = d("event_horizon.radius", 12, 3, 48);
    public static final DoubleValue EH_DURATION = d("event_horizon.duration_seconds", 6, 1, 60);
    public static final DoubleValue EH_REFLECT_CHANCE = d("event_horizon.reflect_chance", 0.3, 0, 1);
    public static final DoubleValue EH_SLOW = d("event_horizon.slow_factor", 0.1, 0, 1);

    // === Gravitational Wave (Level 3 - AOE) ===
    public static final DoubleValue GW_CP = d("gravitational_wave.cp_cost", 3500, 0, 50000);
    public static final DoubleValue GW_OVERLOAD = d("gravitational_wave.overload_cost", 120, 0, 5000);
    public static final IntValue GW_COOLDOWN = i("gravitational_wave.cooldown_seconds", 60, 0, 1800);
    public static final DoubleValue GW_RADIUS = d("gravitational_wave.radius", 24, 5, 64);
    public static final DoubleValue GW_DMG = d("gravitational_wave.damage", 12, 0, 500);
    public static final DoubleValue GW_KB = d("gravitational_wave.knockback", 3.0, 0, 10);
    public static final DoubleValue GW_WAVES = d("gravitational_wave.wave_count", 3, 1, 8);
    public static final DoubleValue GW_DELAY = d("gravitational_wave.wave_delay", 0.4, 0.1, 2);

    // === Shared ===
    public static final DoubleValue BOSS_PULL = d("shared.boss_pull_fraction", 0.25, 0, 1);
    public static final DoubleValue BOSS_DMG = d("shared.boss_damage_fraction", 0.35, 0, 1);
    public static final DoubleValue BOSS_KB = d("shared.boss_knockback_fraction", 0.3, 0, 1);
    public static final DoubleValue MAX_SPEED = d("shared.max_pull_speed", 1.5, 0.2, 5);
    public static final DoubleValue SWIRL = d("shared.swirl_strength", 0.05, 0, 0.5);
    public static final DoubleValue PROJECTILE_SLOW = d("shared.projectile_slow", 0.05, 0, 1);

    public static final ForgeConfigSpec SPEC = B.build();

    public static int ticks(DoubleValue seconds) { return (int) Math.round(seconds.get() * 20.0); }
    public static int ticks(IntValue seconds) { return (int) Math.round(seconds.get() * 20.0); }
}