package github.xevira.groves;

import com.terraformersmc.terraform.boat.api.item.TerraformBoatItemHelper;
import com.terraformersmc.terraform.sign.api.block.TerraformHangingSignBlock;
import com.terraformersmc.terraform.sign.api.block.TerraformSignBlock;
import com.terraformersmc.terraform.sign.api.block.TerraformWallHangingSignBlock;
import com.terraformersmc.terraform.sign.api.block.TerraformWallSignBlock;
import com.mojang.serialization.MapCodec;
import github.xevira.groves.block.*;
import github.xevira.groves.block.entity.*;
import github.xevira.groves.entity.passive.DruidEntity;
import github.xevira.groves.fluid.BlessedMoonWaterFluid;
import github.xevira.groves.fluid.FluidSystem;
import github.xevira.groves.fluid.MoonlightFluid;
import github.xevira.groves.item.*;
import github.xevira.groves.network.GrovesSanctuaryScreenPayload;
import github.xevira.groves.network.MoonwellScreenPayload;
import github.xevira.groves.network.Networking;
import github.xevira.groves.poi.GrovesPOI;
import github.xevira.groves.sanctuary.GroveAbilities;
import github.xevira.groves.sanctuary.GroveAbility;
import github.xevira.groves.sanctuary.GroveUnlocks;
import github.xevira.groves.screenhandler.GrovesSanctuaryScreenHandler;
import github.xevira.groves.screenhandler.MoonwellScreenHandler;
import github.xevira.groves.util.LunarPhasesEnum;
import github.xevira.groves.worldgen.foliage.SanctumFoliagePlacer;
import github.xevira.groves.worldgen.trunk.SanctumTrunkPlacer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistryEvents;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.object.builder.v1.block.type.WoodTypeBuilder;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.component.ComponentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.structure.rule.TagMatchRuleTest;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.foliage.FoliagePlacerType;
import net.minecraft.world.gen.placementmodifier.*;
import net.minecraft.world.gen.stateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.trunk.TrunkPlacer;
import net.minecraft.world.gen.trunk.TrunkPlacerType;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class Registration {
    public static final Map<Item, Item> BLOCK_TO_BLESSED = new HashMap<>();

    @SuppressWarnings("UnstableApiUsage")
    public static final AttachmentType<GrovesPOI.ClientGroveSanctuaryColorData> SANCTUARY_COLOR_DATA =
            AttachmentRegistry.createPersistent(Groves.id("sanctuary_color_data"),
                    GrovesPOI.ClientGroveSanctuaryColorData.CODEC);

    // BlockSetTypes
    public static final BlockSetType SANCTUM_BLOCKSET = new BlockSetType(Groves.id("sanctum").toString());

    // Fluids
    public static final FlowableFluid BLESSED_MOON_WATER_FLUID = register("blessed_moon_water", new BlessedMoonWaterFluid.Still());
    public static final FlowableFluid FLOWING_BLESSED_MOON_WATER_FLUID = register("flowing_blessed_moon_water", new BlessedMoonWaterFluid.Flowing());
    public static final FlowableFluid FLOWING_MOONLIGHT_FLUID = register("flowing_moonlight", new MoonlightFluid.Flowing());
    public static final FlowableFluid MOONLIGHT_FLUID = register("moonlight", new MoonlightFluid.Still());

    // Jukebox Songs
    public static final RegistryKey<JukeboxSong> INTO_THE_HEART_OF_THE_UNIVERSE_KEY =
            RegistryKey.of(RegistryKeys.JUKEBOX_SONG, Groves.id("into_the_heart_of_the_universe"));


    // Sound Events
    public static final SoundEvent DRUID_APPEARED_SOUND = register("druid_appeared");
    public static final SoundEvent DRUID_DEATH_SOUND = register("druid_death");
    public static final SoundEvent DRUID_DISAPPEARED_SOUND = register("druid_disappeared");
    public static final SoundEvent DRUID_DRINK_MILK_SOUND = register("druid_drink_milk");
    public static final SoundEvent DRUID_DRINK_POTION_SOUND = register("druid_drink_potion");
    public static final SoundEvent DRUID_TRADE_SOUND = register("druid_trade");
    public static final SoundEvent DRUID_HURT_SOUND = register("druid_hurt");
    public static final SoundEvent DRUID_AMBIENT_SOUND = register("druid_ambient");
    public static final SoundEvent DRUID_NO_SOUND = register("druid_no");
    public static final SoundEvent DRUID_REAPPEARED_SOUND = register("druid_reappeared");
    public static final SoundEvent DRUID_YES_SOUND = register("druid_yes");

    public static final SoundEvent MOONWELL_ACTIVATE_SOUND = register("moonwell_activate");
    public static final SoundEvent MOONWELL_DEACTIVATE_SOUND = register("moonwell_deactivate");
    public static final SoundEvent INTO_THE_HEART_OF_THE_UNIVERSE_SOUND = register("into_the_heart_of_the_universe");
    public static final RegistryEntry<SoundEvent> MACE_THUNDERING_SOUND = registerReference("mace_thundering");

    public static final SoundEvent WIND_CHIME_BREAK_SOUND = register("wind_chime_break");
    public static final SoundEvent WIND_CHIME_PLACE_SOUND = register("wind_chime_place");
    public static final SoundEvent WIND_CHIME_STEP_SOUND = register("wind_chime_step");
    public static final SoundEvent WIND_CHIME_HIT_SOUND = register("wind_chime_hit");
    public static final SoundEvent WIND_CHIME_FALL_SOUND = register("wind_chime_fall");
    public static final SoundEvent WIND_CHIME_COLLIDE_SOUND = register("wind_chime_collide");
    public static final SoundEvent WIND_CHIME_PROTECT_SOUND = register("wind_chime_protect");

    public static final BlockSoundGroup WIND_CHIME_BLOCK_SOUNDS = new BlockSoundGroup(
            1.0F,
            1.0F,
            WIND_CHIME_BREAK_SOUND,
            WIND_CHIME_STEP_SOUND,
            WIND_CHIME_PLACE_SOUND,
            WIND_CHIME_HIT_SOUND,
            WIND_CHIME_FALL_SOUND
    );

    // Tags
    // -- Block
    /** Valid blocks allowed in the formation of a {@code Moonwell} **/
    public static final TagKey<Block> MOONSTONE_BLOCKS = registerBlockTag("moonstone_blocks");

    /** Valid blocks that compose a {@code Moonwell} **/
    public static final TagKey<Block> MOONWELL_BLOCKS = registerBlockTag("moonwell_blocks");

    public static final TagKey<Block> MOONWELL_CONSTRUCTION_BLOCKS = registerBlockTag("moonwell_construction_blocks");

    /** Valid blocks that compose a {@code Moonwell} for interacting with the screen **/
    public static final TagKey<Block> MOONWELL_INTERACTION_BLOCKS = registerBlockTag("moonwell_interaction_blocks");

    public static final TagKey<Block> SANCTUM_LOG_BLOCKS = registerBlockTag("sanctum_log_blocks");

    public static final TagKey<Item> SANCTUM_PLANKS_ITEMS = registerItemTag("sanctum_planks_items");

    public static final TagKey<Item> SANCTUM_LOG_ITEMS = registerItemTag("sanctum_log_items");

    /** Valid fluids considered {@code Blessed Moon Water} **/
    public static final TagKey<Fluid> BLESSED_MOON_WATERS_TAG = registerFluidTag("blessed_moon_waters");

    /** Valid fluids considered {@code Moonlight} **/
    public static final TagKey<Fluid> MOONLIGHT_TAG = registerFluidTag("moonlight");


    // Boats
    public static final Identifier SANCTUM_BOAT_ID = Groves.id("sanctum");

    // Worldgen

    // - Foliage Placers
    public static final FoliagePlacerType<SanctumFoliagePlacer> SANCTUM_FOLIAGE_PLACER = registerFoliagePlacer("sanctum_foliage_placer", SanctumFoliagePlacer.CODEC);

    // - Trunk Placers
    public static final TrunkPlacerType<SanctumTrunkPlacer> SANCTUM_TRUNK_PLACER = registerTrunkPlacer("sanctum_trunk_placer", SanctumTrunkPlacer.CODEC);

    // - Configured Features
    public static final RegistryKey<ConfiguredFeature<?, ?>> AQUAMARINE_ORE_CONFIG_KEY = registerConfigKey("aquamarine_ore");
    public static final RegistryKey<ConfiguredFeature<?, ?>> SANCTUM_TREE_CONFIG_KEY = registerConfigKey("sanctum_tree");

    // - Placed Features
    public static final RegistryKey<PlacedFeature> AQUAMARINE_ORE_PLACED_KEY = registerPlacedKey("aquamarine_ore");
    public static final RegistryKey<PlacedFeature> SANCTUM_TREE_PLACED_KEY = registerPlacedKey("sanctum_tree");

    // Block Settings
    public static final AbstractBlock.Settings MOONSTONE_SETTINGS = AbstractBlock.Settings.create()
            .mapColor(MapColor.LIGHT_BLUE_GRAY)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresTool()
            .strength(1.5F, 6.0F);

    // Blocks
    public static final Block AQUAMARINE_BLOCK_BLOCK = register(
            "aquamarine_block",
            Block::new,
            AbstractBlock.Settings.create().mapColor(MapColor.BRIGHT_TEAL).instrument(NoteBlockInstrument.BIT).requiresTool().strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL)
    );

    // -- Ores
    public static final Block AQUAMARINE_ORE_BLOCK = register(
            "aquamarine_ore",
            settings -> new ExperienceDroppingBlock(UniformIntProvider.create(3, 7), settings),
            AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(3.0F, 3.0F)
    );

    public static final Block SANCTUM_LOG_BLOCK = register(
            "sanctum_log",
            PillarBlock::new,
            Blocks.createLogSettings(MapColor.ORANGE, MapColor.ORANGE, BlockSoundGroup.WOOD).requiresTool());

    public static final Block SANCTUM_CORE_LOG_BLOCK = register(
            "sanctum_core_log",
            PillarBlock::new,
            Blocks.createLogSettings(MapColor.WHITE_GRAY, MapColor.ORANGE, BlockSoundGroup.WOOD).requiresTool());

    public static final Block SANCTUM_WOOD_BLOCK = register(
            "sanctum_wood",
            PillarBlock::new,
            Blocks.createLogSettings(MapColor.ORANGE, MapColor.ORANGE, BlockSoundGroup.WOOD).requiresTool());

    public static final Block STRIPPED_SANCTUM_LOG_BLOCK = register(
            "stripped_sanctum_log",
            PillarBlock::new,
            Blocks.createLogSettings(MapColor.ORANGE, MapColor.ORANGE, BlockSoundGroup.WOOD).requiresTool());

    public static final Block STRIPPED_SANCTUM_WOOD_BLOCK = register(
            "stripped_sanctum_wood",
            PillarBlock::new,
            Blocks.createLogSettings(MapColor.ORANGE, MapColor.ORANGE, BlockSoundGroup.WOOD).requiresTool());

    public static final Block SANCTUM_LEAVES_BLOCK = register(
            "sanctum_leaves",
            LeavesBlock::new,
            Blocks.createLeavesSettings(BlockSoundGroup.GRASS));

    public static final Block SANCTUM_SAPLING_BLOCK = register(
            "sanctum_sapling",
            settings -> new SanctumSaplingBlock(
                    new SaplingGenerator(
                            Groves.id("sanctum").toString(),
                            0.0f,
                            Optional.of(SANCTUM_TREE_CONFIG_KEY),
                            Optional.empty(),   // TODO: Make the variant that has iron wood in it, but make it super rare
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty()
                    ),
                    settings
            ),
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.ORANGE)
                    .ticksRandomly()
                    .strength(0.0F)
                    .sounds(BlockSoundGroup.GRASS)
                    .nonOpaque()
                    .allowsSpawning(Blocks::canSpawnOnLeaves)
                    .suffocates(Blocks::never)
                    .blockVision(Blocks::never)
                    .burnable()
                    .pistonBehavior(PistonBehavior.DESTROY)
                    .solidBlock(Blocks::never)
                    .noCollision()
    );

    public static final Block POTTED_SANCTUM_SAPLING_BLOCK = register(
            "potted_sanctun_sapling",
            settings -> new FlowerPotBlock(SANCTUM_SAPLING_BLOCK, settings),
            Blocks.createFlowerPotSettings());


    public static final Block SANCTUM_PLANKS_BLOCK = register(
            "sanctum_planks",
            Block::new,
            AbstractBlock.Settings.create().mapColor(MapColor.ORANGE).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sounds(BlockSoundGroup.WOOD).burnable()
    );

    public static final Block SANCTUM_SLAB_BLOCK = register(
            "sanctum_slab",
            SlabBlock::new,
            AbstractBlock.Settings.create().mapColor(MapColor.ORANGE).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sounds(BlockSoundGroup.WOOD).burnable()
    );

    public static final Block SANCTUM_BUTTON_BLOCK = register(
            "sanctum_button",
            settings -> new ButtonBlock(SANCTUM_BLOCKSET, 30, settings),
            Blocks.createButtonSettings()
    );

    @SuppressWarnings("deprecation")
    public static final Block SANCTUM_STAIRS_BLOCK = register(
            "sanctum_stairs",
            settings -> new StairsBlock(SANCTUM_PLANKS_BLOCK.getDefaultState(), settings),
            AbstractBlock.Settings.copyShallow(SANCTUM_PLANKS_BLOCK)
    );

    public static final Block SANCTUM_DOOR_BLOCK = register(
            "sanctum_door",
            settings -> new DoorBlock(SANCTUM_BLOCKSET, settings),
            AbstractBlock.Settings.create()
                    .mapColor(SANCTUM_PLANKS_BLOCK.getDefaultMapColor())
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .nonOpaque()
                    .burnable()
                    .pistonBehavior(PistonBehavior.DESTROY)
    );

    public static final Block SANCTUM_TRAPDOOR_BLOCK = register(
            "sanctum_trapdoor",
            settings -> new TrapdoorBlock(SANCTUM_BLOCKSET, settings),
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.ORANGE)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .nonOpaque()
                    .allowsSpawning(Blocks::never)
                    .burnable()
    );

    public static final Block SANCTUM_SIGN_BLOCK = register(
            "sanctum_sign",
            settings -> new TerraformSignBlock(Groves.id("entity/signs/sanctum"), settings),
            AbstractBlock.Settings.create().mapColor(MapColor.ORANGE).solid().instrument(NoteBlockInstrument.BASS).noCollision().strength(1.0F).burnable()
    );

    public static final Block SANCTUM_WALL_SIGN_BLOCK = register(
            "sanctum_wall_sign",
            settings -> new TerraformWallSignBlock(Groves.id("entity/signs/sanctum"), settings),
            copyLootTable(SANCTUM_SIGN_BLOCK, true).mapColor(MapColor.ORANGE).solid().instrument(NoteBlockInstrument.BASS).noCollision().strength(1.0F).burnable()
    );

    public static final Block SANCTUM_HANGING_SIGN_BLOCK = register(
            "sanctum_hanging_sign",
            settings -> new TerraformHangingSignBlock(Groves.id("entity/signs/hanging/sanctum"), Groves.id("textures/gui/hanging_signs/sanctum"), settings),
            AbstractBlock.Settings.create().mapColor(SANCTUM_LOG_BLOCK.getDefaultMapColor()).solid().instrument(NoteBlockInstrument.BASS).noCollision().strength(1.0F).burnable()
    );

    public static final Block SANCTUM_WALL_HANGING_SIGN_BLOCK = register(
            "sanctum_wall_hanging_sign",
            settings -> new TerraformWallHangingSignBlock(Groves.id("entity/signs/hanging/sanctum"), Groves.id("textures/gui/hanging_signs/sanctum"), settings),
            copyLootTable(SANCTUM_HANGING_SIGN_BLOCK, true)
                    .mapColor(SANCTUM_LOG_BLOCK.getDefaultMapColor())
                    .solid()
                    .instrument(NoteBlockInstrument.BASS)
                    .noCollision()
                    .strength(1.0F)
                    .burnable()
    );

    public static final Block SANCTUM_PRESSURE_PLATE_BLOCK = register(
            "sanctum_pressure_plate",
            settings -> new PressurePlateBlock(SANCTUM_BLOCKSET, settings),
            AbstractBlock.Settings.create()
                    .mapColor(SANCTUM_PLANKS_BLOCK.getDefaultMapColor())
                    .solid()
                    .instrument(NoteBlockInstrument.BASS)
                    .noCollision()
                    .strength(0.5F)
                    .burnable()
                    .pistonBehavior(PistonBehavior.DESTROY)
    );

    public static final Block SANCTUM_FENCE_BLOCK = register(
            "sanctum_fence",
            FenceBlock::new,
            AbstractBlock.Settings.create()
                    .mapColor(SANCTUM_PLANKS_BLOCK.getDefaultMapColor())
                    .solid()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.0F, 3.0F)
                    .sounds(BlockSoundGroup.WOOD)
                    .burnable()
    );

    public static final Block SANCTUM_FENCE_GATE_BLOCK = register(
            "sanctum_fence_gate",
            settings -> new FenceGateBlock(WoodType.OAK, settings),
            AbstractBlock.Settings.create().mapColor(SANCTUM_PLANKS_BLOCK.getDefaultMapColor()).solid().instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).burnable()
    );


    @SuppressWarnings("deprecation")        // Vanilla uses copyShallow.
    public static final Block DEEPSLATE_AQUAMARINE_ORE_BLOCK = register(
            "deepslate_aquamarine_ore",
            settings -> new ExperienceDroppingBlock(UniformIntProvider.create(3, 7), settings),
            AbstractBlock.Settings.copyShallow(AQUAMARINE_ORE_BLOCK).mapColor(MapColor.DEEPSLATE_GRAY).strength(4.5F, 3.0F).sounds(BlockSoundGroup.DEEPSLATE)
    );

    // -- Fluid Blocks
    public static final Block BLESSED_MOON_WATER_BLOCK = register(
            "blessed_moon_water",
            settings -> new BlessedMoonWaterBlock(BLESSED_MOON_WATER_FLUID, settings),
            AbstractBlock.Settings.create()
                .mapColor(MapColor.LIGHT_BLUE)
                .replaceable()
                .noCollision()
                .strength(100.0F)
                .pistonBehavior(PistonBehavior.DESTROY)
                .dropsNothing()
                .liquid()
                .sounds(BlockSoundGroup.INTENTIONALLY_EMPTY));

    public static final Block MOONLIGHT_BLOCK = register(
            "moonlight",
            settings -> new MoonlightBlock(MOONLIGHT_FLUID, settings),
            AbstractBlock.Settings.create()
                .mapColor(MapColor.WHITE)
                .replaceable()
                .noCollision()
                .strength(100.0F)
                .pistonBehavior(PistonBehavior.DESTROY)
                .dropsNothing()
                .liquid()
                .sounds(BlockSoundGroup.INTENTIONALLY_EMPTY));

    // -- Moonstone
    public static final Block CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK = register(
            "chiseled_moonstone_bricks_full_moon",
            settings -> new ChiseledMoonstoneBricksBlock(LunarPhasesEnum.FULL_MOON, settings),
            MOONSTONE_SETTINGS);

    public static final Block CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK = register(
            "chiseled_moonstone_bricks_waning_gibbous",
            settings -> new ChiseledMoonstoneBricksBlock(LunarPhasesEnum.WANING_GIBBOUS,settings),
            MOONSTONE_SETTINGS);

    public static final Block CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK = register(
            "chiseled_moonstone_bricks_third_quarter",
            settings -> new ChiseledMoonstoneBricksBlock(LunarPhasesEnum.THIRD_QUARTER,settings),
            MOONSTONE_SETTINGS);

    public static final Block CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK = register(
            "chiseled_moonstone_bricks_waning_crescent",
            settings -> new ChiseledMoonstoneBricksBlock(LunarPhasesEnum.WANING_CRESCENT, settings),
            MOONSTONE_SETTINGS);

    public static final Block CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK = register(
            "chiseled_moonstone_bricks_new_moon",
            settings -> new ChiseledMoonstoneBricksBlock(LunarPhasesEnum.NEW_MOON,settings),
            MOONSTONE_SETTINGS);

    public static final Block CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK = register(
            "chiseled_moonstone_bricks_waxing_crescent",
            settings -> new ChiseledMoonstoneBricksBlock(LunarPhasesEnum.WAXING_CRESCENT,settings),
            MOONSTONE_SETTINGS);

    public static final Block CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK = register(
            "chiseled_moonstone_bricks_first_quarter",
            settings -> new ChiseledMoonstoneBricksBlock(LunarPhasesEnum.FIRST_QUARTER,settings),
            MOONSTONE_SETTINGS);

    public static final Block CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK = register(
            "chiseled_moonstone_bricks_waxing_gibbous",
            settings -> new ChiseledMoonstoneBricksBlock(LunarPhasesEnum.WAXING_GIBBOUS,settings),
            MOONSTONE_SETTINGS);

    public static final Block CRACKED_MOONSTONE_BRICKS_BLOCK = register("cracked_moonstone_bricks", MoonstoneBrickBlock::new, MOONSTONE_SETTINGS);

    public static final Block MOONSTONE_BRICKS_BLOCK = register("moonstone_bricks", MoonstoneBrickBlock::new, MOONSTONE_SETTINGS);

    public static final Block MOONSTONE_BRICK_SLAB_BLOCK = register("moonstone_brick_slab", MoonstoneBrickSlabBlock::new, MOONSTONE_SETTINGS);

    public static final Block MOONSTONE_BRICK_STAIRS_BLOCK = register(
            "moonstone_brick_stairs",
            settings -> new MoonstoneBrickStairBlock(MOONSTONE_BRICKS_BLOCK.getDefaultState(), settings),
            MOONSTONE_SETTINGS);

    public static final Block MOONSTONE_BRICK_WALL_BLOCK = register("moonstone_brick_wall", MoonstoneBrickWallBlock::new, MOONSTONE_SETTINGS);

    // -- Moonwell
    // TODO: Figure out how to make this block get ignored by raycasting
    public static final Block MOONWELL_FAKE_FLUID_BLOCK = register( "moonwell_fake_fluid", MoonwellFakeFluidBlock::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.CLEAR)
                    .instrument(NoteBlockInstrument.CHIME)
                    .strength(-1.0F, 3600000.0F)
                    .dropsNothing()
                    .allowsSpawning(Blocks::never)
                    .nonOpaque()
                    .noCollision()
                    .solidBlock(Blocks::never)
                    .suffocates(Blocks::never)
                    .blockVision(Blocks::never)
                    .luminance(state -> 8)
                    .air());

    public static final Block CHISELED_MOONWELL_BRICKS_FULL_MOON_BLOCK = register(
            "chiseled_moonwell_bricks_full_moon",
            settings -> new ChiseledMoonwellBricksBlock(LunarPhasesEnum.FULL_MOON, settings),
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .lootTable(CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK.getLootTableKey())
                    .requiresTool().
                    strength(2.0F, 6.0F));

    public static final Block CHISELED_MOONWELL_BRICKS_WANING_GIBBOUS_BLOCK = register(
            "chiseled_moonwell_bricks_waning_gibbous",
            settings -> new ChiseledMoonwellBricksBlock(LunarPhasesEnum.WANING_GIBBOUS, settings),
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .lootTable(CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK.getLootTableKey())
                    .requiresTool().
                    strength(2.0F, 6.0F));

    public static final Block CHISELED_MOONWELL_BRICKS_THIRD_QUARTER_BLOCK = register(
            "chiseled_moonwell_bricks_third_quarter",
            settings -> new ChiseledMoonwellBricksBlock(LunarPhasesEnum.THIRD_QUARTER, settings),
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .lootTable(CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK.getLootTableKey())
                    .requiresTool().
                    strength(2.0F, 6.0F));

    public static final Block CHISELED_MOONWELL_BRICKS_WANING_CRESCENT_BLOCK = register(
            "chiseled_moonwell_bricks_waning_crescent",
            settings -> new ChiseledMoonwellBricksBlock(LunarPhasesEnum.WANING_CRESCENT,settings),
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .lootTable(CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK.getLootTableKey())
                    .requiresTool().
                    strength(2.0F, 6.0F));

    public static final Block CHISELED_MOONWELL_BRICKS_NEW_MOON_BLOCK = register(
            "chiseled_moonwell_bricks_new_moon",
            settings -> new ChiseledMoonwellBricksBlock(LunarPhasesEnum.NEW_MOON,settings),
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .lootTable(CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK.getLootTableKey())
                    .requiresTool().
                    strength(2.0F, 6.0F));

    public static final Block CHISELED_MOONWELL_BRICKS_WAXING_CRESCENT_BLOCK = register(
            "chiseled_moonwell_bricks_waxing_crescent",
            settings -> new ChiseledMoonwellBricksBlock(LunarPhasesEnum.WAXING_CRESCENT,settings),
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .lootTable(CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK.getLootTableKey())
                    .requiresTool().
                    strength(2.0F, 6.0F));

    public static final Block CHISELED_MOONWELL_BRICKS_FIRST_QUARTER_BLOCK = register(
            "chiseled_moonwell_bricks_first_quarter",
            settings -> new ChiseledMoonwellBricksBlock(LunarPhasesEnum.FIRST_QUARTER,settings),
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .lootTable(CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK.getLootTableKey())
                    .requiresTool().
                    strength(2.0F, 6.0F));

    public static final Block CHISELED_MOONWELL_BRICKS_WAXING_GIBBOUS_BLOCK = register(
            "chiseled_moonwell_bricks_waxing_gibbous",
            settings -> new ChiseledMoonwellBricksBlock(LunarPhasesEnum.WANING_GIBBOUS, settings),
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .lootTable(CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK.getLootTableKey())
                    .requiresTool().
                    strength(2.0F, 6.0F));

    public static final Block CRACKED_MOONWELL_BRICKS_BLOCK = register("cracked_moonwell_bricks",MoonwellBrickBlock::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .lootTable(CRACKED_MOONSTONE_BRICKS_BLOCK.getLootTableKey())
                    .requiresTool().
                    strength(2.0F, 6.0F));

    public static final Block MOONWELL_BASIN_BLOCK = register("moonwell_basin", MoonwellBasinBlock::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .lootTable(Blocks.CAULDRON.getLootTableKey())
                    .requiresTool()
                    .strength(2.0F, 6.0F));

    public static final Block MOONWELL_BRICKS_BLOCK = register("moonwell_bricks", MoonwellBrickBlock::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .lootTable(MOONSTONE_BRICKS_BLOCK.getLootTableKey())
                    .requiresTool().
                    strength(2.0F, 6.0F));

    public static final Block MOONWELL_BRICK_SLAB_BLOCK = register("moonwell_brick_slab", MoonwellBrickSlabBlock::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .lootTable(MOONSTONE_BRICK_SLAB_BLOCK.getLootTableKey())
                    .requiresTool().
                    strength(2.0F, 6.0F));

    public static final Block MOONWELL_BRICK_STAIRS_BLOCK = register(
            "moonwell_brick_stairs",
            settings -> new MoonwellBrickStairBlock(MOONWELL_BRICKS_BLOCK.getDefaultState(), settings),
                AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .lootTable(MOONSTONE_BRICK_STAIRS_BLOCK.getLootTableKey())
                    .requiresTool().
                    strength(2.0F, 6.0F));

    public static final Block MOONWELL_BRICK_WALL_BLOCK = register("moonwell_brick_wall", MoonwellBrickWallBlock::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .lootTable(MOONSTONE_BRICK_WALL_BLOCK.getLootTableKey())
                    .requiresTool().
                    strength(2.0F, 6.0F)
    );

    public static final Block WIND_CHIME_BLOCK = register("wind_chime",
            settings -> new WindChimeBlock(100, settings),
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.ORANGE)
                    .instrument(NoteBlockInstrument.CHIME)
                    .sounds(WIND_CHIME_BLOCK_SOUNDS)
                    .strength(2.0F, 6.0F)
            );

    public static final Block WORN_WIND_CHIME_BLOCK = register("worn_wind_chime",
            settings -> new WindChimeBlock(50, settings),
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.ORANGE)
                    .instrument(NoteBlockInstrument.CHIME)
                    .sounds(WIND_CHIME_BLOCK_SOUNDS)
                    .strength(2.0F, 6.0F)
    );

    public static final Block DAMAGED_WIND_CHIME_BLOCK = register("damaged_wind_chime",
            settings -> new WindChimeBlock(0, settings),
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.ORANGE)
                    .instrument(NoteBlockInstrument.CHIME)
                    .sounds(WIND_CHIME_BLOCK_SOUNDS)
                    .strength(2.0F, 6.0F)
    );


    // -- Waxed Moonstone
    public static final Block WAXED_CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK = register(
            "waxed_chiseled_moonstone_bricks_full_moon",
            settings -> new WaxedChiseledMoonstoneBricksBlock(LunarPhasesEnum.FULL_MOON,settings),
            MOONSTONE_SETTINGS);

    public static final Block WAXED_CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK = register(
            "waxed_chiseled_moonstone_bricks_waning_gibbous",
            settings -> new WaxedChiseledMoonstoneBricksBlock(LunarPhasesEnum.WANING_GIBBOUS,settings),
            MOONSTONE_SETTINGS);

    public static final Block WAXED_CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK = register(
            "waxed_chiseled_moonstone_bricks_third_quarter",
            settings -> new WaxedChiseledMoonstoneBricksBlock(LunarPhasesEnum.THIRD_QUARTER,settings),
            MOONSTONE_SETTINGS);

    public static final Block WAXED_CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK = register(
            "waxed_chiseled_moonstone_bricks_waning_crescent",
            settings -> new WaxedChiseledMoonstoneBricksBlock(LunarPhasesEnum.WANING_CRESCENT,settings),
            MOONSTONE_SETTINGS);

    public static final Block WAXED_CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK = register(
            "waxed_chiseled_moonstone_bricks_new_moon",
            settings -> new WaxedChiseledMoonstoneBricksBlock(LunarPhasesEnum.NEW_MOON,settings),
            MOONSTONE_SETTINGS);

    public static final Block WAXED_CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK = register(
            "waxed_chiseled_moonstone_bricks_waxing_crescent",
            settings -> new WaxedChiseledMoonstoneBricksBlock(LunarPhasesEnum.WAXING_CRESCENT,settings),
            MOONSTONE_SETTINGS);

    public static final Block WAXED_CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK = register(
            "waxed_chiseled_moonstone_bricks_first_quarter",
            settings -> new WaxedChiseledMoonstoneBricksBlock(LunarPhasesEnum.FULL_MOON,settings),
            MOONSTONE_SETTINGS);

    public static final Block WAXED_CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK = register(
            "waxed_chiseled_moonstone_bricks_waxing_gibbous",
            settings -> new WaxedChiseledMoonstoneBricksBlock(LunarPhasesEnum.WAXING_GIBBOUS,settings),
            MOONSTONE_SETTINGS);

    public static final Block WAXED_CRACKED_MOONSTONE_BRICKS_BLOCK = register("waxed_cracked_moonstone_bricks", Block::new,MOONSTONE_SETTINGS);

    public static final Block WAXED_MOONSTONE_BRICKS_BLOCK = register("waxed_moonstone_bricks", Block::new, MOONSTONE_SETTINGS);

    public static final Block WAXED_MOONSTONE_BRICK_SLAB_BLOCK = register("waxed_moonstone_brick_slab", SlabBlock::new, MOONSTONE_SETTINGS);

    public static final Block WAXED_MOONSTONE_BRICK_STAIRS_BLOCK = register("waxed_moonstone_brick_stairs",
            settings -> new StairsBlock(WAXED_MOONSTONE_BRICKS_BLOCK.getDefaultState(),settings),
            MOONSTONE_SETTINGS);

    public static final Block WAXED_MOONSTONE_BRICK_WALL_BLOCK = register("waxed_moonstone_brick_wall", WallBlock::new, MOONSTONE_SETTINGS);

    // BlockItems
    public static final Item SANCTUM_LOG_ITEM = register(SANCTUM_LOG_BLOCK);

    public static final Item SANCTUM_CORE_LOG_ITEM = register(SANCTUM_CORE_LOG_BLOCK);

    public static final Item SANCTUM_WOOD_ITEM = register(SANCTUM_WOOD_BLOCK);

    public static final Item STRIPPED_SANCTUM_LOG_ITEM = register(STRIPPED_SANCTUM_LOG_BLOCK);

    public static final Item STRIPPED_SANCTUM_WOOD_ITEM = register(STRIPPED_SANCTUM_WOOD_BLOCK);

    public static final Item SANCTUM_LEAVES_ITEM = register(SANCTUM_LEAVES_BLOCK);

    public static final Item SANCTUM_SAPLING_ITEM = register(SANCTUM_SAPLING_BLOCK);

    public static final Item SANCTUM_PLANKS_ITEM = register(SANCTUM_PLANKS_BLOCK);

    public static final Item SANCTUM_SLAB_ITEM = register(SANCTUM_SLAB_BLOCK);

    public static final Item SANCTUM_BUTTON_ITEM = register(SANCTUM_BUTTON_BLOCK);

    public static final Item SANCTUM_STAIRS_ITEM = register(SANCTUM_STAIRS_BLOCK);

    public static final Item SANCTUM_DOOR_ITEM = register(SANCTUM_DOOR_BLOCK);

    public static final Item SANCTUM_TRAPDOOR_ITEM = register(SANCTUM_TRAPDOOR_BLOCK);

    public static final SignItem SANCTUM_SIGN_ITEM = register(
            "sanctum_sign",
            settings -> new SignItem(SANCTUM_SIGN_BLOCK, SANCTUM_WALL_SIGN_BLOCK, settings),
            new Item.Settings().maxCount(16)
    );

    public static final HangingSignItem SANCTUM_HANGING_SIGN_ITEM = register(
            "sanctum_hanging_sign",
            settings -> new HangingSignItem(SANCTUM_HANGING_SIGN_BLOCK, SANCTUM_WALL_HANGING_SIGN_BLOCK, settings),
            new Item.Settings().maxCount(16)
    );

    public static final Item SANCTUM_PRESSURE_PLATE_ITEM = register(SANCTUM_PRESSURE_PLATE_BLOCK);

    public static final Item SANCTUM_FENCE_ITEM = register(SANCTUM_FENCE_BLOCK);

    public static final Item SANCTUM_FENCE_GATE_ITEM = register(SANCTUM_FENCE_GATE_BLOCK);

    public static final Item SANCTUM_BOAT_ITEM = TerraformBoatItemHelper.registerBoatItem(SANCTUM_BOAT_ID, false);

    public static final Item SANCTUM_CHEST_BOAT_ITEM = TerraformBoatItemHelper.registerBoatItem(SANCTUM_BOAT_ID, true);

    // Sanctum wood is harder than normal wood
    public static final ToolMaterial SANCTUM_TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_STONE_TOOL, 95, 4.0F, 1.0F, 15, SANCTUM_PLANKS_ITEMS);

    public static final Item SANCTUM_SWORD_ITEM = register(
            "sanctum_sword",
            settings -> new SwordItem(SANCTUM_TOOL_MATERIAL, 3.0F, -2.4F, settings),
            new Item.Settings()
    );

    public static final Item SANCTUM_PICKAXE_ITEM = register(
            "sanctum_pickaxe",
            settings -> new PickaxeItem(SANCTUM_TOOL_MATERIAL, 1.0F, -2.8F, settings),
            new Item.Settings()
    );

    public static final Item SANCTUM_AXE_ITEM = register(
            "sanctum_axe",
            settings -> new AxeItem(SANCTUM_TOOL_MATERIAL, 7.0F, -3.2F, settings),
            new Item.Settings()
    );

    public static final Item SANCTUM_SHOVEL_ITEM = register(
            "sanctum_shovel",
            settings -> new ShovelItem(SANCTUM_TOOL_MATERIAL, 1.5F, -3.0F, settings),
            new Item.Settings()
    );

    public static final Item SANCTUM_HOE_ITEM = register(
            "sanctum_hoe",
            settings -> new HoeItem(SANCTUM_TOOL_MATERIAL, -1.0F, -2.0F, settings),
            new Item.Settings()
    );

    public static final Item AQUAMARINE_ORE_ITEM = register(AQUAMARINE_ORE_BLOCK);

    public static final Item AQUAMARINE_BLOCK_ITEM = register(AQUAMARINE_BLOCK_BLOCK);

    public static final Item BLESSED_MOON_WATER_ITEM = register(BLESSED_MOON_WATER_BLOCK);

    public static final Item DEEPSLATE_AQUAMARINE_ORE_ITEM = register(DEEPSLATE_AQUAMARINE_ORE_BLOCK);

    public static final Item MOONLIGHT_ITEM = register(MOONLIGHT_BLOCK);

    public static final Item CHISELED_MOONSTONE_BRICKS_FULL_MOON_ITEM = register(CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK);

    public static final Item CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_ITEM = register(CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK);

    public static final Item CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_ITEM = register(CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK);

    public static final Item CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_ITEM = register(CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK);

    public static final Item CHISELED_MOONSTONE_BRICKS_NEW_MOON_ITEM = register(CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK);

    public static final Item CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_ITEM = register(CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK);

    public static final Item CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_ITEM = register(CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK);

    public static final Item CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_ITEM = register(CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK);

    public static final Item CRACKED_MOONSTONE_BRICKS_ITEM = register(CRACKED_MOONSTONE_BRICKS_BLOCK);

    public static final Item MOONSTONE_BRICKS_ITEM = register(MOONSTONE_BRICKS_BLOCK);

    public static final Item MOONSTONE_BRICK_SLAB_ITEM = register(MOONSTONE_BRICK_SLAB_BLOCK);

    public static final Item MOONSTONE_BRICK_STAIRS_ITEM = register(MOONSTONE_BRICK_STAIRS_BLOCK);

    public static final Item MOONSTONE_BRICK_WALL_ITEM = register(MOONSTONE_BRICK_WALL_BLOCK);

    public static final Item MOONWELL_BRICKS_ITEM = register(MOONWELL_BRICKS_BLOCK);

    public static final Item MOONWELL_BRICK_SLAB_ITEM = register(MOONWELL_BRICK_SLAB_BLOCK);

    public static final Item MOONWELL_BRICK_STAIRS_ITEM = register(MOONWELL_BRICK_STAIRS_BLOCK);

    public static final Item MOONWELL_BRICK_WALL_ITEM = register(MOONWELL_BRICK_WALL_BLOCK);

    public static final Item WAXED_CHISELED_MOONSTONE_BRICKS_FULL_MOON_ITEM = register(WAXED_CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK);

    public static final Item WAXED_CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_ITEM = register(WAXED_CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK);

    public static final Item WAXED_CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_ITEM = register(WAXED_CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK);

    public static final Item WAXED_CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_ITEM = register(WAXED_CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK);

    public static final Item WAXED_CHISELED_MOONSTONE_BRICKS_NEW_MOON_ITEM = register(WAXED_CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK);

    public static final Item WAXED_CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_ITEM = register(WAXED_CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK);

    public static final Item WAXED_CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_ITEM = register(WAXED_CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK);

    public static final Item WAXED_CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_ITEM = register(WAXED_CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK);

    public static final Item WAXED_CRACKED_MOONSTONE_BRICKS_ITEM = register(WAXED_CRACKED_MOONSTONE_BRICKS_BLOCK);

    public static final Item WAXED_MOONSTONE_BRICKS_ITEM = register(WAXED_MOONSTONE_BRICKS_BLOCK);

    public static final Item WAXED_MOONSTONE_BRICK_SLAB_ITEM = register(WAXED_MOONSTONE_BRICK_SLAB_BLOCK);

    public static final Item WAXED_MOONSTONE_BRICK_STAIRS_ITEM = register(WAXED_MOONSTONE_BRICK_STAIRS_BLOCK);

    public static final Item WAXED_MOONSTONE_BRICK_WALL_ITEM = register(WAXED_MOONSTONE_BRICK_WALL_BLOCK);

    public static final Item WIND_CHIME_ITEM = register(WIND_CHIME_BLOCK);

    public static final Item WORN_WIND_CHIME_ITEM = register(WORN_WIND_CHIME_BLOCK);

    public static final Item DAMAGED_WIND_CHIME_ITEM = register(DAMAGED_WIND_CHIME_BLOCK);

    // Items
    public static final Item AQUAMARINE_ITEM = register(
            "aquamarine",
            Item::new,
            new Item.Settings().maxCount(64));

    public static final Item AQUAMARINE_DUST_ITEM = register(
            "aquamarine_dust",
            Item::new,
            new Item.Settings().maxCount(64));

    public static final Item BLESSED_MOON_WATER_BUCKET_ITEM = register(
            "blessed_moon_water_bucket",
            settings -> new BucketItem(BLESSED_MOON_WATER_FLUID, settings),
            new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1));

    public static final Item ENCHANTED_IMPRINTING_SIGIL_ITEM = register(
            "enchanted_imprinting_sigil",
            settings -> new ImprintingSigilItem(true, settings),
            new Item.Settings().rarity(Rarity.EPIC).maxCount(1).fireproof());

    public static final Item IMPRINTING_SIGIL_ITEM = register(
            "imprinting_sigil",
            settings -> new ImprintingSigilItem(false, settings),
            new Item.Settings().rarity(Rarity.RARE).maxCount(1).fireproof());

    public static final Item INTO_THE_HEART_OF_THE_UNIVERSE_MUSIC_DISC_ITEM = register(
            "into_the_heart_of_the_universe_music_disc",
            Item::new,
            new Item.Settings().rarity(Rarity.EPIC).jukeboxPlayable(INTO_THE_HEART_OF_THE_UNIVERSE_KEY).maxCount(1));

    public static final Item IRONWOOD_SHARD_ITEM = register(
            "ironwood_shard",
            Item::new,
            new Item.Settings().maxCount(64));

    public static final Item MOONLIGHT_BUCKET_ITEM = register(
            "moonlight_bucket",
            settings -> new BucketItem(MOONLIGHT_FLUID, settings),
            new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1));

    public static final Item MOON_PHIAL_ITEM = register(
            "moon_phial",
            MoonPhialItem::new,
            new Item.Settings().maxCount(16).rarity(Rarity.RARE));

    public static final Item UNLOCK_SCROLL_ITEM = register(
            "unlock_scroll",
            settings -> new UnlockScrollItem(null, 0, settings),
            new Item.Settings().maxCount(64));

    // This item should *never* be loaded without an ability assigned to it
    public static final Item FORBIDDEN_SCROLL_ITEM = register(
            "forbidden_scroll",
            settings -> new UnlockScrollItem(null, 0, settings),
            new Item.Settings().maxCount(64));

    // Block Entities
    public static final BlockEntityType<MoonwellMultiblockMasterBlockEntity> MOONWELL_MULTIBLOCK_MASTER_BLOCK_ENTITY = register("moonwell_master",
            FabricBlockEntityTypeBuilder.create(MoonwellMultiblockMasterBlockEntity::new, MOONWELL_BASIN_BLOCK)
                    .build());

    public static final BlockEntityType<MoonwellMultiblockSlaveBlockEntity> MOONWELL_MULTIBLOCK_SLAVE_BLOCK_ENTITY = register("moonwell_slave",
            FabricBlockEntityTypeBuilder.create(MoonwellMultiblockSlaveBlockEntity::new,
                            MOONWELL_FAKE_FLUID_BLOCK,
                            CHISELED_MOONWELL_BRICKS_FULL_MOON_BLOCK,
                            CHISELED_MOONWELL_BRICKS_WANING_GIBBOUS_BLOCK,
                            CHISELED_MOONWELL_BRICKS_THIRD_QUARTER_BLOCK,
                            CHISELED_MOONWELL_BRICKS_WANING_CRESCENT_BLOCK,
                            CHISELED_MOONWELL_BRICKS_NEW_MOON_BLOCK,
                            CHISELED_MOONWELL_BRICKS_WAXING_GIBBOUS_BLOCK,
                            CHISELED_MOONWELL_BRICKS_FIRST_QUARTER_BLOCK,
                            CHISELED_MOONWELL_BRICKS_WAXING_CRESCENT_BLOCK,
                            CRACKED_MOONWELL_BRICKS_BLOCK,
                            MOONWELL_BRICKS_BLOCK,
                            MOONWELL_BRICK_SLAB_BLOCK,
                            MOONWELL_BRICK_STAIRS_BLOCK,
                            MOONWELL_BRICK_WALL_BLOCK)
                    .build());


    public static final BlockEntityType<MoonwellFakeFluidBlockEntity> MOONWELL_FAKE_FLUID_BLOCK_ENTITY = register("moonwell_fake_fluid",
            FabricBlockEntityTypeBuilder.create(MoonwellFakeFluidBlockEntity::new,
                            MOONWELL_FAKE_FLUID_BLOCK)
                    .build());

    // Entities
    public static final EntityType<DruidEntity> DRUID_ENTITY = register("druid",
            EntityType.Builder.create(DruidEntity::new, SpawnGroup.CREATURE)
                    .dimensions(0.6F, 1.95F)
                    .eyeHeight(1.62F)
                    .maxTrackingRange(10));

    // Screen Handlers
    public static final ScreenHandlerType<MoonwellScreenHandler> MOONWELL_SCREEN_HANDLER = register("moonwell", MoonwellScreenHandler::new, MoonwellScreenPayload.PACKET_CODEC);
    public static final ScreenHandlerType<GrovesSanctuaryScreenHandler> GROVES_SANCTUARY_SCREEN_HANDLER = register("groves_sanctuary", GrovesSanctuaryScreenHandler::new, GrovesSanctuaryScreenPayload.PACKET_CODEC);


    // Item Groups
    public static final ItemGroup GROVES_ITEM_GROUP = registerItemGroup("groves_items", MOONSTONE_BRICKS_ITEM, "groves_items",
            // Ores
            AQUAMARINE_ORE_ITEM,
            DEEPSLATE_AQUAMARINE_ORE_ITEM,

            // Trees and Wood
            SANCTUM_LOG_ITEM,
            SANCTUM_CORE_LOG_ITEM,
            SANCTUM_WOOD_ITEM,
            STRIPPED_SANCTUM_LOG_ITEM,
            STRIPPED_SANCTUM_WOOD_ITEM,
            SANCTUM_LEAVES_ITEM,
            SANCTUM_SAPLING_ITEM,

            SANCTUM_PLANKS_ITEM,
            SANCTUM_SLAB_ITEM,
            SANCTUM_STAIRS_ITEM,
            SANCTUM_DOOR_ITEM,
            SANCTUM_TRAPDOOR_ITEM,
            SANCTUM_BUTTON_ITEM,
            SANCTUM_PRESSURE_PLATE_ITEM,
            SANCTUM_FENCE_ITEM,
            SANCTUM_FENCE_GATE_ITEM,
            SANCTUM_SIGN_ITEM,
            SANCTUM_HANGING_SIGN_ITEM,
            SANCTUM_BOAT_ITEM,
            SANCTUM_CHEST_BOAT_ITEM,

            SANCTUM_SWORD_ITEM,
            SANCTUM_PICKAXE_ITEM,
            SANCTUM_AXE_ITEM,
            SANCTUM_SHOVEL_ITEM,
            SANCTUM_HOE_ITEM,

            // Moonstone blocks
            MOONSTONE_BRICKS_ITEM,
            CRACKED_MOONSTONE_BRICKS_ITEM,
            CHISELED_MOONSTONE_BRICKS_FULL_MOON_ITEM,
            CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_ITEM,
            CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_ITEM,
            CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_ITEM,
            CHISELED_MOONSTONE_BRICKS_NEW_MOON_ITEM,
            CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_ITEM,
            CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_ITEM,
            CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_ITEM,
            MOONSTONE_BRICK_SLAB_ITEM,
            MOONSTONE_BRICK_STAIRS_ITEM,
            MOONSTONE_BRICK_WALL_ITEM,

            // Waxed blocks
            WAXED_MOONSTONE_BRICKS_ITEM,
            WAXED_CRACKED_MOONSTONE_BRICKS_ITEM,
            WAXED_CHISELED_MOONSTONE_BRICKS_FULL_MOON_ITEM,
            WAXED_CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_ITEM,
            WAXED_CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_ITEM,
            WAXED_CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_ITEM,
            WAXED_CHISELED_MOONSTONE_BRICKS_NEW_MOON_ITEM,
            WAXED_CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_ITEM,
            WAXED_CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_ITEM,
            WAXED_CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_ITEM,
            WAXED_MOONSTONE_BRICK_SLAB_ITEM,
            WAXED_MOONSTONE_BRICK_STAIRS_ITEM,
            WAXED_MOONSTONE_BRICK_WALL_ITEM,

            // Items
            AQUAMARINE_ITEM,
            AQUAMARINE_DUST_ITEM,
            AQUAMARINE_BLOCK_ITEM,
            BLESSED_MOON_WATER_BUCKET_ITEM,
            MOONLIGHT_BUCKET_ITEM,
            MOON_PHIAL_ITEM,
            IMPRINTING_SIGIL_ITEM,
            ENCHANTED_IMPRINTING_SIGIL_ITEM,
            INTO_THE_HEART_OF_THE_UNIVERSE_MUSIC_DISC_ITEM,
            IRONWOOD_SHARD_ITEM,
            UNLOCK_SCROLL_ITEM,
            WIND_CHIME_ITEM,
            WORN_WIND_CHIME_ITEM,
            DAMAGED_WIND_CHIME_ITEM
    );

    public static final FluidSystem BLESSED_MOON_WATER_FLUID_DATA = new FluidSystem.Builder(BLESSED_MOON_WATERS_TAG)
            .preventsBlockSpreading()
            .canSwim()
            .fluidMovementSpeed((entity, speed) -> 0.5f)
            .applyWaterMovement()
            .canCauseDrowning()
            .shouldWitchDrinkWaterBreathing()
            .shouldEvaporateInUltrawarm()
            .affectsBlockBreakSpeed()
            .canBoatsWork()
            .build();

    public static final FluidSystem MOONLIGHT_FLUID_DATA = new FluidSystem.Builder(MOONLIGHT_TAG)
            .preventsBlockSpreading()
            .canSwim()
            .fluidMovementSpeed((entity, speed) -> 0.5f)
            .applyWaterMovement()
            .shouldEvaporateInUltrawarm()
            .affectsBlockBreakSpeed()
            .canBoatsWork()
            .build();

    public static final RegistryKey<Enchantment> LIGHT_FOOTED_ENCHANTMENT_KEY = RegistryKey.of(RegistryKeys.ENCHANTMENT, Groves.id("light_footed"));
    public static final RegistryKey<Enchantment> SOLAR_REPAIR_ENCHANTMENT_KEY = RegistryKey.of(RegistryKeys.ENCHANTMENT, Groves.id("solar_repair"));
    public static final RegistryKey<Enchantment> THUNDERING_ENCHANTMENT_KEY = RegistryKey.of(RegistryKeys.ENCHANTMENT, Groves.id("thundering"));

    // Registration Functions
    public static Block register(RegistryKey<Block> key, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        Block block = (Block)factory.apply(settings.registryKey(key));
        return Registry.register(Registries.BLOCK, key, block);
    }

    public static Block register(RegistryKey<Block> key, AbstractBlock.Settings settings) {
        return register(key, Block::new, settings);
    }

    private static RegistryKey<Block> keyOf(String id) {
        return RegistryKey.of(RegistryKeys.BLOCK, Groves.id(id));
    }

    private static Block register(String id, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        return register(keyOf(id), factory, settings);
    }

    private static Block register(String id, AbstractBlock.Settings settings) {
        return register(id, Block::new, settings);
    }

    public static <T extends FlowableFluid> T register(String name, T fluid) {
        return Registry.register(Registries.FLUID, Groves.id(name), fluid);
    }

//    public static <T extends Block> T register(String name, T block) {
//        return Registry.register(Registries.BLOCK, Groves.id(name), block);
//    }
//
//    public static <T extends Block> T registerWithItem(String name, T block, Item.Settings settings) {
//        T registered = register(name, block);
//        register(name, new BlockItem(registered, settings));
//        return registered;
//    }
//
//    public static <T extends Block> T registerWithItem(String name, T block) {
//        return registerWithItem(name, block, new Item.Settings());
//    }

    public static <T extends Item> T register(String name, Function<Item.Settings, T> constructor, Function<Item.Settings, Item.Settings> settingsApplier) {
        return register(name, constructor.apply(
                settingsApplier.apply(new Item.Settings().registryKey(
                        RegistryKey.of(RegistryKeys.ITEM, Groves.id(name))))));
    }

    public static <T extends Item> T register(String name, Function<Item.Settings, T> constructor, Item.Settings settings) {
        return register(name, constructor.apply(settings.registryKey(RegistryKey.of(RegistryKeys.ITEM, Groves.id(name)))));
    }

    public static Item register(Block block) {
        return register(block, BlockItem::new);
    }

    private static RegistryKey<Item> keyOf(RegistryKey<Block> blockKey) {
        return RegistryKey.of(RegistryKeys.ITEM, blockKey.getValue());
    }

    public static Item register(Block block, BiFunction<Block, Item.Settings, Item> factory) {
        return register(block, factory, new Item.Settings());
    }

    @SuppressWarnings("deprecation")
    public static Item register(Block block, BiFunction<Block, Item.Settings, Item> factory, Item.Settings settings) {
        return register(
                keyOf(block.getRegistryEntry().registryKey()),
                itemSettings -> (Item)factory.apply(block, itemSettings), settings.useBlockPrefixedTranslationKey()
        );
    }

    public static Item register(RegistryKey<Item> key, Function<Item.Settings, Item> factory, Item.Settings settings) {
        Item item = (Item)factory.apply(settings.registryKey(key));
        if (item instanceof BlockItem blockItem) {
            blockItem.appendBlocks(Item.BLOCK_ITEMS, item);
        }

        return Registry.register(Registries.ITEM, key, item);
    }

    public static <T extends Item> T register(String name, T item)
    {
        return Registry.register(Registries.ITEM, Groves.id(name), item);
    }

    public static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType<T> type) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Groves.id(name), type);
    }

    public static SoundEvent register(String name)
    {
        Identifier id = Groves.id(name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    private static RegistryEntry.Reference<SoundEvent> registerReference(String name) {
        Identifier id = Groves.id(name);
        return Registry.registerReference(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }


    public static <T extends ScreenHandler, D extends CustomPayload> ExtendedScreenHandlerType<T, D> register(String name, ExtendedScreenHandlerType.ExtendedFactory<T, D> factory, PacketCodec<? super RegistryByteBuf, D> codec) {
        return Registry.register(Registries.SCREEN_HANDLER, Groves.id(name), new ExtendedScreenHandlerType<>(factory, codec));
    }

    public static TagKey<Block> registerBlockTag(String name) {
        return TagKey.of(RegistryKeys.BLOCK, Groves.id(name));
    }

    public static TagKey<Item> registerItemTag(String name) {
        return TagKey.of(RegistryKeys.ITEM, Groves.id(name));
    }

    public static TagKey<Fluid> registerFluidTag(String name) {
        return TagKey.of(RegistryKeys.FLUID, Groves.id(name));
    }

    public static <T> ComponentType<T> registerComponent(String name, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, Groves.id(name),
                builderOperator.apply(ComponentType.builder()).build());
    }

    public static SimpleParticleType registerParticle(String name, boolean alwaysShow)
    {
        return Registry.register(Registries.PARTICLE_TYPE, Groves.id(name), FabricParticleTypes.simple(alwaysShow));
    }

    public static ItemGroup registerItemGroup(String name, Item icon, String display, Item... items)
    {
        return Registry.register(Registries.ITEM_GROUP, Groves.id(name),
                FabricItemGroup.builder()
                        .icon(() -> new ItemStack(icon))
                        .displayName(Text.translatable(Groves.textPath("itemgroup", display)))
                        .entries((displayContext, entries) -> Arrays.stream(items).forEach(entries::add)).build());
    }

    private static <T extends Entity> EntityType<T> register(RegistryKey<EntityType<?>> key, EntityType.Builder<T> type) {
        return Registry.register(Registries.ENTITY_TYPE, key, type.build(key));
    }

    private static <T extends Entity> EntityType<T> register(String id, EntityType.Builder<T> type) {
        RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Groves.id(id));
        return register(key, type);
    }

    public static <P extends FoliagePlacer> FoliagePlacerType<P> registerFoliagePlacer(String id, MapCodec<P> codec) {
        return Registry.register(Registries.FOLIAGE_PLACER_TYPE, id, new FoliagePlacerType<>(codec));
    }

    public static <P extends TrunkPlacer> TrunkPlacerType<P> registerTrunkPlacer(String id, MapCodec<P> codec) {
        return Registry.register(Registries.TRUNK_PLACER_TYPE, id, new TrunkPlacerType<>(codec));
    }

    public static RegistryKey<ConfiguredFeature<?, ?>> registerConfigKey(String name) {
        return RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Groves.id(name));
    }

    public static RegistryKey<PlacedFeature> registerPlacedKey(String name) {
        return RegistryKey.of(RegistryKeys.PLACED_FEATURE, Groves.id(name));
    }


    public static <FC extends FeatureConfig, F extends Feature<FC>> void registerConfiguredFeature(Registerable<ConfiguredFeature<?, ?>> context,
                                                                                   RegistryKey<ConfiguredFeature<?, ?>> key,
                                                                                   F feature,
                                                                                   FC featureConfig) {
        context.register(key, new ConfiguredFeature<>(feature, featureConfig));
    }

    public static void registerPlacedFeature(Registerable<PlacedFeature> context,
                                 RegistryKey<PlacedFeature> key,
                                 RegistryEntry<ConfiguredFeature<?, ?>> config,
                                 List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(config, List.copyOf(modifiers)));
    }

    private static AbstractBlock.Settings copyLootTable(Block block, boolean copyTranslationKey) {
        AbstractBlock.Settings settings = block.getSettings();
        AbstractBlock.Settings settings2 = AbstractBlock.Settings.create().lootTable(block.getLootTableKey());
        if (copyTranslationKey) {
            settings2 = settings2.overrideTranslationKey(block.getTranslationKey());
        }

        return settings2;
    }

    public static void bootstrapConfiguredFeature(Registerable<ConfiguredFeature<?, ?>> context)
    {
        RuleTest stoneOreReplaceables = new TagMatchRuleTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateOreReplaceables = new TagMatchRuleTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        List<OreFeatureConfig.Target> overworldAquamarineTargets = List.of(
                OreFeatureConfig.createTarget(stoneOreReplaceables, AQUAMARINE_ORE_BLOCK.getDefaultState()),
                OreFeatureConfig.createTarget(deepslateOreReplaceables, DEEPSLATE_AQUAMARINE_ORE_BLOCK.getDefaultState()));

        registerConfiguredFeature(context, AQUAMARINE_ORE_CONFIG_KEY, Feature.ORE, new OreFeatureConfig(overworldAquamarineTargets, 9));

        registerConfiguredFeature(context, SANCTUM_TREE_CONFIG_KEY, Feature.TREE, new TreeFeatureConfig.Builder(
                SimpleBlockStateProvider.of(SANCTUM_LOG_BLOCK),
                new SanctumTrunkPlacer(
                        30,
                        10,
                        10,
                        UniformIntProvider.create(3, 5),
                        UniformIntProvider.create(3, 6),
                        UniformIntProvider.create(12,16)
                ),
                SimpleBlockStateProvider.of(SANCTUM_LEAVES_BLOCK),
                new SanctumFoliagePlacer(ConstantIntProvider.create(4), ConstantIntProvider.create(0), 5),
                new TwoLayersFeatureSize(1, 0, 2)
        ).ignoreVines().build());
    }

    public static void bootstrapPlacedFeature(Registerable<PlacedFeature> context)
    {
        RegistryEntryLookup<ConfiguredFeature<?, ?>> registryLookup = context.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);

        registerPlacedFeature(context, AQUAMARINE_ORE_PLACED_KEY, registryLookup.getOrThrow(AQUAMARINE_ORE_CONFIG_KEY),
                Modifiers.modifiersCount(9,
                        HeightRangePlacementModifier.uniform(YOffset.fixed(-8), YOffset.fixed(24))));

        registerPlacedFeature(context, SANCTUM_TREE_PLACED_KEY, registryLookup.getOrThrow(SANCTUM_TREE_CONFIG_KEY),
                VegetationPlacedFeatures.treeModifiersWithWouldSurvive(
                        PlacedFeatures.createCountExtraModifier(1, 0.1f, 0),
                        SANCTUM_SAPLING_BLOCK));
    }


    private static void registerFlammables()
    {
        FlammableBlockRegistry flammableRegistry = FlammableBlockRegistry.getDefaultInstance();

        flammableRegistry.add(SANCTUM_PLANKS_BLOCK, 5, 20);
        flammableRegistry.add(SANCTUM_SLAB_BLOCK, 5, 20);
        flammableRegistry.add(SANCTUM_STAIRS_BLOCK, 5, 20);
        flammableRegistry.add(SANCTUM_FENCE_BLOCK, 5, 20);
        flammableRegistry.add(SANCTUM_FENCE_GATE_BLOCK, 5, 20);
        flammableRegistry.add(SANCTUM_LOG_BLOCK, 5, 5);
        flammableRegistry.add(STRIPPED_SANCTUM_LOG_BLOCK, 5, 5);
        flammableRegistry.add(STRIPPED_SANCTUM_WOOD_BLOCK, 5, 5);
        flammableRegistry.add(SANCTUM_WOOD_BLOCK, 5, 5);
        flammableRegistry.add(SANCTUM_CORE_LOG_BLOCK, 5, 5);
        flammableRegistry.add(SANCTUM_LEAVES_BLOCK, 30, 60);
    }

    private static void registerStrippables()
    {
        StrippableBlockRegistry.register(SANCTUM_LOG_BLOCK, STRIPPED_SANCTUM_LOG_BLOCK);
        StrippableBlockRegistry.register(SANCTUM_WOOD_BLOCK, STRIPPED_SANCTUM_WOOD_BLOCK);
    }

    private static void registerCompostables()
    {
        CompostingChanceRegistry compostingRegistry = CompostingChanceRegistry.INSTANCE;
        float LEAVES_CHANCE = compostingRegistry.get(Items.OAK_LEAVES);
        float SAPLING_CHANCE = compostingRegistry.get(Items.OAK_SAPLING);

        compostingRegistry.add(SANCTUM_LEAVES_ITEM, LEAVES_CHANCE);
        compostingRegistry.add(SANCTUM_SAPLING_ITEM, SAPLING_CHANCE);
    }

    private static void registerFuels()
    {
        FuelRegistryEvents.BUILD.register((builder, context) -> {
            builder.add(SANCTUM_LOG_ITEM, 300);
            builder.add(SANCTUM_WOOD_ITEM, 300);
            builder.add(STRIPPED_SANCTUM_LOG_ITEM, 300);
            builder.add(STRIPPED_SANCTUM_WOOD_ITEM, 300);
            builder.add(SANCTUM_PLANKS_ITEM, 300);
            builder.add(SANCTUM_SLAB_ITEM, 150);
            builder.add(SANCTUM_STAIRS_ITEM, 300);
            builder.add(SANCTUM_BUTTON_ITEM, 100);
            builder.add(SANCTUM_PRESSURE_PLATE_ITEM, 300);
            builder.add(SANCTUM_DOOR_ITEM, 200);
            builder.add(SANCTUM_TRAPDOOR_ITEM, 300);
            builder.add(SANCTUM_FENCE_ITEM, 300);
            builder.add(SANCTUM_FENCE_GATE_ITEM, 300);
            builder.add(SANCTUM_SIGN_ITEM, 200);
            builder.add(SANCTUM_HANGING_SIGN_ITEM, 200);
            builder.add(SANCTUM_SAPLING_ITEM, 100);
            // TODO: Add sanctum tools.
        });
    }

    private static void registerBlessings()
    {
        BLOCK_TO_BLESSED.put(Items.STONE_BRICKS, Registration.MOONSTONE_BRICKS_ITEM);
        BLOCK_TO_BLESSED.put(Items.STONE_BRICK_SLAB, Registration.MOONSTONE_BRICK_SLAB_ITEM);
        BLOCK_TO_BLESSED.put(Items.STONE_BRICK_WALL, Registration.MOONSTONE_BRICK_WALL_ITEM);
        BLOCK_TO_BLESSED.put(Items.CRACKED_STONE_BRICKS, Registration.CRACKED_MOONSTONE_BRICKS_ITEM);
        BLOCK_TO_BLESSED.put(Items.CHISELED_STONE_BRICKS, Registration.CHISELED_MOONSTONE_BRICKS_FULL_MOON_ITEM);
    }

    private static void registerFluidData()
    {
        FluidSystem.registerFluid(BLESSED_MOON_WATER_FLUID, BLESSED_MOON_WATER_FLUID_DATA);
        FluidSystem.registerFluid(FLOWING_BLESSED_MOON_WATER_FLUID, BLESSED_MOON_WATER_FLUID_DATA);
        FluidSystem.registerFluid(MOONLIGHT_FLUID, MOONLIGHT_FLUID_DATA);
        FluidSystem.registerFluid(FLOWING_MOONLIGHT_FLUID, MOONLIGHT_FLUID_DATA);
    }

    private static void registerBiomeData()
    {
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                AQUAMARINE_ORE_PLACED_KEY
        );
    }

    private static void registerEntityData()
    {
        SpawnRestriction.register(DRUID_ENTITY, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canMobSpawn);
        FabricDefaultAttributeRegistry.register(DRUID_ENTITY, DruidEntity.addAttributes());
    }

    public static void load() {
        registerFlammables();
        registerStrippables();
        registerCompostables();
        registerFuels();
        registerBlessings();
        registerFluidData();
        registerBiomeData();
        registerEntityData();


        GroveAbilities.register();
        GroveUnlocks.register();

        // TODO: Add items to other item groups

        Networking.register();
    }

    public static class Modifiers {
        public static List<PlacementModifier> modifiers(PlacementModifier countModifier, PlacementModifier heightModifier) {
            return List.of(countModifier, SquarePlacementModifier.of(), heightModifier, BiomePlacementModifier.of());
        }

        public static List<PlacementModifier> modifiersCount(int count, PlacementModifier heightModifier) {
            return modifiers(CountPlacementModifier.of(count), heightModifier);
        }

        public static List<PlacementModifier> modifiersRarity(int chance, PlacementModifier heightModifier) {
            return modifiers(RarityFilterPlacementModifier.of(chance), heightModifier);
        }
    }
}
