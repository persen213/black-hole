# Black Hole Ultimate – Re:AcademyCraft 1.5.0 add-on (Minecraft 1.20.1, Forge 47.x)

Six unique black hole abilities in the **Singularity** category, spanning levels 2–5. A separate mod — does not modify Re:AcademyCraft.

## Abilities

| Level | Ability | Description |
|-------|---------|-------------|
| 2 | **Micro Black Hole** | Tiny, fast utility singularity. 8-block pull, 3s active, quick cooldown. Great for quick CC. |
| 3 | **Black Hole (Classic)** | Balanced standard. 20-block pull, 6s active, 18-block collapse blast. Reliable workhorse. |
| 3 | **Gravitational Wave** | Emits 3 expanding force rings. 24-block radius, high knockback, no pull. Pure AOE. |
| 4 | **Quasar** | Hybrid offense. 24-block pull + damaging beam (30 range, 25 dmg/tick). 8s active. |
| 4 | **Event Horizon** | Defensive dome. 12-block radius, slows projectiles 90%, 30% reflect chance. Protects allies. |
| 5 | **Void Singularity** | **Ultimate.** 32-block pull, 10s active, true damage inside horizon, 30-block collapse blast. Devours everything. |

## Mechanics

- **Formation phase**: Visual expands from hand to full radius (0.4–2s depending on variant)
- **Active phase**: Pulls hostile entities/projectiles, damages inside event horizon
- **Collapse**: Shockwave with damage falloff + knockback (except Event Horizon = gentle fade)
- **Safety**: Uses Re:AcademyCraft's `AcademySafety` — respects PvP/teams/protection, never hurts allies
- **Boss handling**: Reduced pull/damage/knockback (configurable)
- **Cleanup**: Auto-cancels on death, logout, dimension change, server stop
- **No tick freezing** — only scales entity `deltaMovement`
- **All numbers configurable** in `config/blackholeultimate-common.toml`

## Visuals — Zero Particles

All custom 3D geometry rendered via `VertexConsumer`:
- **Micro**: Small core, fast violet rings, quick collapse
- **Classic**: Photon ring, accretion disk, infall streams, gravitational waves, implosion
- **Quasar**: Energy rings + pulsing beam downward, bright collapse
- **Void**: Dark matter spirals, void tendrils, dark lensing, massive implosion
- **Event Horizon**: Hexagonal shield pattern, protective shimmer, gentle fade
- **Gravitational Wave**: Expanding ripple rings, final massive wave

Screen effects: subtle vignette when inside any field.

## Build on GitHub (no Java needed)

1. Create private repo `blackhole-ultimate`
2. Unzip `blackhole-ultimate.zip`, upload everything inside `blackhole-ultimate` folder
3. Actions tab → paste `.github/workflows/build.yml` if needed → commit
4. Run workflow → download `blackhole-ultimate-mod` artifact → unzip → `blackhole-ultimate-forge-1.20.1-1.0.0.jar`

## Install

1. Forge 47.4.10+ / Minecraft 1.20.1 instance
2. `mods/` folder: Re:AcademyCraft 1.5.0 + `blackhole-ultimate-forge-1.20.1-1.0.0.jar`
3. Creative → Ingredients tab → **Singularity Factor** → Ability Developer → learn skills → bind keys
4. Server: same jars on server + all clients

## Files

- `BlackHoleUltimateMod.java` — registration, 6 skills, Singularity category
- `Cfg.java` — 50+ config values (every number externalized)
- `mechanics/` — `BlackHoleBase` + 6 variants (Micro, Classic, Quasar, Void, EventHorizon, GravitationalWave)
- `net/` — `SimpleChannel` with `FxStartMsg`/`FxCancelMsg` (nearby players, dimension cancel)
- `client/` — `FxRenderer` (6 variant renderers), `Geo` (procedural geometry), `BhRenderTypes` (5 render types), `ClientFx` (effect registry)
- `Targeting.java` — aiming, validation, safety checks
- `.github/workflows/build.yml` — GitHub Actions build