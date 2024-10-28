package github.xevira.groves;

import github.xevira.groves.block.*;
import github.xevira.groves.block.entity.*;
import github.xevira.groves.fluid.BlessedMoonWaterFluid;
import github.xevira.groves.fluid.FluidSystem;
import github.xevira.groves.fluid.MoonlightFluid;
import github.xevira.groves.item.*;
import github.xevira.groves.network.GrovesSanctuaryScreenPayload;
import github.xevira.groves.network.MoonwellScreenPayload;
import github.xevira.groves.network.Networking;
import github.xevira.groves.screenhandler.GrovesSanctuaryScreenHandler;
import github.xevira.groves.screenhandler.MoonwellScreenHandler;
import github.xevira.groves.util.LunarPhasesEnum;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.component.ComponentType;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class Registration {
    // Fluids
    public static final FlowableFluid BLESSED_MOON_WATER_FLUID = register("blessed_moon_water", new BlessedMoonWaterFluid.Still());
    public static final FlowableFluid FLOWING_BLESSED_MOON_WATER_FLUID = register("flowing_blessed_moon_water", new BlessedMoonWaterFluid.Flowing());
    public static final FlowableFluid FLOWING_MOONLIGHT_FLUID = register("flowing_moonlight", new MoonlightFluid.Flowing());
    public static final FlowableFluid MOONLIGHT_FLUID = register("moonlight", new MoonlightFluid.Still());

    // Block Settings
    public static final AbstractBlock.Settings MOONSTONE_SETTINGS = AbstractBlock.Settings.create()
            .mapColor(MapColor.LIGHT_BLUE_GRAY)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresTool()
            .strength(1.5F, 6.0F);

    // Blocks
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
                    strength(2.0F, 6.0F));


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
    public static final Item BLESSED_MOON_WATER_ITEM = register(BLESSED_MOON_WATER_BLOCK);

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

    // Items
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

    public static final Item MOONLIGHT_BUCKET_ITEM = register(
            "moonlight_bucket",
            settings -> new BucketItem(MOONLIGHT_FLUID, settings),
            new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1));

    public static final Item MOON_PHIAL_ITEM = register(
            "moon_phial",
            MoonPhialItem::new,
            new Item.Settings().maxCount(16).rarity(Rarity.RARE));

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

    // Screen Handlers
    public static final ScreenHandlerType<MoonwellScreenHandler> MOONWELL_SCREEN_HANDLER = register("moonwell", MoonwellScreenHandler::new, MoonwellScreenPayload.PACKET_CODEC);
    public static final ScreenHandlerType<GrovesSanctuaryScreenHandler> GROVES_SANCTUARY_SCREEN_HANDLER = register("groves_sanctuary", GrovesSanctuaryScreenHandler::new, GrovesSanctuaryScreenPayload.PACKET_CODEC);

    // Sound Events
    public static final SoundEvent MOONWELL_ACTIVATE_SOUND = register("moonwell_activate");
    public static final SoundEvent MOONWELL_DEACTIVATE_SOUND = register("moonwell_deactivate");

    // Tags
    // -- Block
    /** Valid blocks allowed in the formation of a {@code Moonwell} **/
    public static final TagKey<Block> MOONSTONE_BLOCKS = registerBlockTag("moonstone_blocks");

    /** Valid blocks that compose a {@code Moonwell} **/
    public static final TagKey<Block> MOONWELL_BLOCKS = registerBlockTag("moonwell_blocks");

    public static final TagKey<Block> MOONWELL_CONSTRUCTION_BLOCKS = registerBlockTag("moonwell_construction_blocks");

    /** Valid blocks that compose a {@code Moonwell} for interacting with the screen **/
    public static final TagKey<Block> MOONWELL_INTERACTION_BLOCKS = registerBlockTag("moonwell_interaction_blocks");

    /** Valid fluids considered {@code Blessed Moon Water} **/
    public static final TagKey<Fluid> BLESSED_MOON_WATERS_TAG = registerFluidTag("blessed_moon_waters");

    /** Valid fluids considered {@code Moonlight} **/
    public static final TagKey<Fluid> MOONLIGHT_TAG = registerFluidTag("moonlight");

    // Item Groups
    public static final ItemGroup GROVES_ITEM_GROUP = registerItemGroup("groves_items", MOONSTONE_BRICKS_ITEM, "groves_items",

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
            BLESSED_MOON_WATER_BUCKET_ITEM,
            MOONLIGHT_BUCKET_ITEM,
            MOON_PHIAL_ITEM,
            IMPRINTING_SIGIL_ITEM,
            ENCHANTED_IMPRINTING_SIGIL_ITEM);

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

    public static final Map<Item, Item> BLOCK_TO_BLESSED = new HashMap<>();

    public static void load() {
        BLOCK_TO_BLESSED.put(Items.STONE_BRICKS, Registration.MOONSTONE_BRICKS_ITEM);
        BLOCK_TO_BLESSED.put(Items.STONE_BRICK_SLAB, Registration.MOONSTONE_BRICK_SLAB_ITEM);
        BLOCK_TO_BLESSED.put(Items.STONE_BRICK_WALL, Registration.MOONSTONE_BRICK_WALL_ITEM);
        BLOCK_TO_BLESSED.put(Items.CRACKED_STONE_BRICKS, Registration.CRACKED_MOONSTONE_BRICKS_ITEM);
        BLOCK_TO_BLESSED.put(Items.CHISELED_STONE_BRICKS, Registration.CHISELED_MOONSTONE_BRICKS_FULL_MOON_ITEM);

        FluidSystem.registerFluid(BLESSED_MOON_WATER_FLUID, BLESSED_MOON_WATER_FLUID_DATA);
        FluidSystem.registerFluid(FLOWING_BLESSED_MOON_WATER_FLUID, BLESSED_MOON_WATER_FLUID_DATA);
        FluidSystem.registerFluid(MOONLIGHT_FLUID, MOONLIGHT_FLUID_DATA);
        FluidSystem.registerFluid(FLOWING_MOONLIGHT_FLUID, MOONLIGHT_FLUID_DATA);

        Networking.register();
    }
}
