package dev.toaru.blackholeultimate;

import com.mojang.logging.LogUtils;
import dev.toaru.blackholeultimate.net.Net;
import io.github.pinchan4273.reacademycraft.api.AbilityId;
import io.github.pinchan4273.reacademycraft.api.ActivationKind;
import io.github.pinchan4273.reacademycraft.api.CategoryDefinition;
import io.github.pinchan4273.reacademycraft.api.Presentation;
import io.github.pinchan4273.reacademycraft.api.SkillDefinition;
import io.github.pinchan4273.reacademycraft.api.TreePosition;
import io.github.pinchan4273.reacademycraft.api.forge.RegisterAbilitiesEvent;
import java.util.List;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

/**
 * Black Hole Ultimate: A Singularity-category ultimate ability for Re:AcademyCraft 1.5.0.
 * Features multiple black hole variants, advanced physics, and stunning visuals.
 */
@Mod(BlackHoleUltimateMod.MOD_ID)
public final class BlackHoleUltimateMod {
    public static final String MOD_ID = "blackholeultimate";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    /** The item that gives the Singularity category. */
    public static final RegistryObject<Item> SINGULARITY_FACTOR = ITEMS.register("singularity_factor", () -> new Item(new Item.Properties().stacksTo(16)));

    public static AbilityId id(String path) { return AbilityId.parse(MOD_ID + ":" + path); }
    public static final AbilityId CATEGORY = id("singularity");

    // Main abilities
    public static final AbilityId BLACK_HOLE_CLASSIC = id("black_hole_classic");
    public static final AbilityId BLACK_HOLE_QUASAR = id("black_hole_quasar");
    public static final AbilityId BLACK_HOLE_MICRO = id("black_hole_micro");
    public static final AbilityId BLACK_HOLE_VOID = id("black_hole_void");
    public static final AbilityId EVENT_HORIZON = id("event_horizon");
    public static final AbilityId GRAVITATIONAL_WAVE = id("gravitational_wave");

    public BlackHoleUltimateMod() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        bus.addListener(this::commonSetup);
        bus.addListener(this::registerAbilities);
        bus.addListener(this::creativeTab);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Cfg.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) { event.enqueueWork(Net::register); }

    private void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) event.accept(SINGULARITY_FACTOR);
    }

    private void registerAbilities(RegisterAbilitiesEvent event) {
        var abilities = event.registrar(MOD_ID);

        // Singularity category with 6 skills across levels 2-5
        abilities.category(new CategoryDefinition(CATEGORY, id("singularity_factor"), List.of(0, 1, 1, 1, 3),
                new Presentation(MOD_ID + ".ability.singularity", null, null)));

        // Level 2: Micro Black Hole - small, fast, utility
        abilities.skill(new SkillDefinition(BLACK_HOLE_MICRO, CATEGORY, 2, List.of(), ActivationKind.TOGGLE,
                new Presentation(MOD_ID + ".skill.black_hole_micro", MOD_ID + ".skill.black_hole_micro.desc", null),
                new TreePosition(80, 40, null)));
        event.behaviors(MOD_ID).behavior(BLACK_HOLE_MICRO, new MicroBlackHole());

        // Level 3: Classic Black Hole - balanced
        abilities.skill(new SkillDefinition(BLACK_HOLE_CLASSIC, CATEGORY, 3, List.of(BLACK_HOLE_MICRO), ActivationKind.TOGGLE,
                new Presentation(MOD_ID + ".skill.black_hole_classic", MOD_ID + ".skill.black_hole_classic.desc", null),
                new TreePosition(110, 55, null)));
        event.behaviors(MOD_ID).behavior(BLACK_HOLE_CLASSIC, new ClassicBlackHole());

        // Level 3: Gravitational Wave - knockback/aoe
        abilities.skill(new SkillDefinition(GRAVITATIONAL_WAVE, CATEGORY, 3, List.of(BLACK_HOLE_MICRO), ActivationKind.TOGGLE,
                new Presentation(MOD_ID + ".skill.gravitational_wave", MOD_ID + ".skill.gravitational_wave.desc", null),
                new TreePosition(140, 40, null)));
        event.behaviors(MOD_ID).behavior(GRAVITATIONAL_WAVE, new GravitationalWave());

        // Level 4: Quasar - beam + pull hybrid
        abilities.skill(new SkillDefinition(BLACK_HOLE_QUASAR, CATEGORY, 4, List.of(BLACK_HOLE_CLASSIC), ActivationKind.TOGGLE,
                new Presentation(MOD_ID + ".skill.black_hole_quasar", MOD_ID + ".skill.black_hole_quasar.desc", null),
                new TreePosition(110, 70, null)));
        event.behaviors(MOD_ID).behavior(BLACK_HOLE_QUASAR, new QuasarBlackHole());

        // Level 4: Event Horizon - defensive zone
        abilities.skill(new SkillDefinition(EVENT_HORIZON, CATEGORY, 4, List.of(BLACK_HOLE_CLASSIC), ActivationKind.TOGGLE,
                new Presentation(MOD_ID + ".skill.event_horizon", MOD_ID + ".skill.event_horizon.desc", null),
                new TreePosition(80, 70, null)));
        event.behaviors(MOD_ID).behavior(EVENT_HORIZON, new EventHorizon());

        // Level 5: Void Black Hole - ultimate
        abilities.skill(new SkillDefinition(BLACK_HOLE_VOID, CATEGORY, 5, List.of(BLACK_HOLE_QUASAR, EVENT_HORIZON), ActivationKind.TOGGLE,
                new Presentation(MOD_ID + ".skill.black_hole_void", MOD_ID + ".skill.black_hole_void.desc", null),
                new TreePosition(110, 85, null)));
        event.behaviors(MOD_ID).behavior(BLACK_HOLE_VOID, new VoidBlackHole());
    }

    @Mod.EventBusSubscriber(modid = MOD_ID)
    public static final class Cleanup {
        private Cleanup() { }

        @SubscribeEvent
        public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
            MicroBlackHole.forget(event.getEntity().getUUID());
            ClassicBlackHole.forget(event.getEntity().getUUID());
            QuasarBlackHole.forget(event.getEntity().getUUID());
            VoidBlackHole.forget(event.getEntity().getUUID());
            EventHorizon.forget(event.getEntity().getUUID());
            GravitationalWave.forget(event.getEntity().getUUID());
        }

        @SubscribeEvent
        public static void onServerStopping(ServerStoppingEvent event) {
            MicroBlackHole.clearAll();
            ClassicBlackHole.clearAll();
            QuasarBlackHole.clearAll();
            VoidBlackHole.clearAll();
            EventHorizon.clearAll();
            GravitationalWave.clearAll();
        }
    }
}