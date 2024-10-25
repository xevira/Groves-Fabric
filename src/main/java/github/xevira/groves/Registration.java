package github.xevira.groves;

import github.xevira.groves.block.*;
import github.xevira.groves.block.entity.*;
import github.xevira.groves.fluid.BlessedMoonWaterFluid;
import github.xevira.groves.item.*;
import github.xevira.groves.util.LunarPhasesEnum;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
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
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public class Registration {
    // Fluids
    public static final FlowableFluid BLESSED_MOON_WATER_FLUID = register("blessed_moon_water", new BlessedMoonWaterFluid.Still());
    public static final FlowableFluid FLOWING_BLESSED_MOON_WATER_FLUID = register("flowing_blessed_moon_water", new BlessedMoonWaterFluid.Flowing());

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
            new BlessedMoonWaterBlock(
                    BLESSED_MOON_WATER_FLUID,
                    AbstractBlock.Settings.create()
                            .mapColor(MapColor.LIGHT_BLUE)
                            .replaceable()
                            .noCollision()
                            .strength(100.0F)
                            .pistonBehavior(PistonBehavior.DESTROY)
                            .dropsNothing()
                            .liquid()
                            .sounds(BlockSoundGroup.INTENTIONALLY_EMPTY)
            )
    );

    // -- Moonstone
    public static final Block CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK = register("chiseled_moonstone_bricks_full_moon", new ChiseledMoonstoneBricksBlock(LunarPhasesEnum.FULL_MOON,MOONSTONE_SETTINGS));

    public static final Block CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK = register("chiseled_moonstone_bricks_waning_gibbous", new ChiseledMoonstoneBricksBlock(LunarPhasesEnum.WANING_GIBBOUS,MOONSTONE_SETTINGS));

    public static final Block CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK = register("chiseled_moonstone_bricks_third_quarter", new ChiseledMoonstoneBricksBlock(LunarPhasesEnum.THIRD_QUARTER,MOONSTONE_SETTINGS));

    public static final Block CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK = register("chiseled_moonstone_bricks_waning_crescent", new ChiseledMoonstoneBricksBlock(LunarPhasesEnum.WANING_CRESCENT,MOONSTONE_SETTINGS));

    public static final Block CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK = register("chiseled_moonstone_bricks_new_moon", new ChiseledMoonstoneBricksBlock(LunarPhasesEnum.NEW_MOON,MOONSTONE_SETTINGS));

    public static final Block CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK = register("chiseled_moonstone_bricks_waxing_crescent", new ChiseledMoonstoneBricksBlock(LunarPhasesEnum.WAXING_CRESCENT,MOONSTONE_SETTINGS));

    public static final Block CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK = register("chiseled_moonstone_bricks_first_quarter", new ChiseledMoonstoneBricksBlock(LunarPhasesEnum.FIRST_QUARTER,MOONSTONE_SETTINGS));

    public static final Block CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK = register("chiseled_moonstone_bricks_waxing_gibbous", new ChiseledMoonstoneBricksBlock(LunarPhasesEnum.WAXING_GIBBOUS,MOONSTONE_SETTINGS));

    public static final Block CRACKED_MOONSTONE_BRICKS_BLOCK = register("cracked_moonstone_bricks", new MoonstoneBrickBlock(MOONSTONE_SETTINGS));

    public static final Block MOONSTONE_BRICKS_BLOCK = register("moonstone_bricks", new MoonstoneBrickBlock(MOONSTONE_SETTINGS));

    public static final Block MOONSTONE_BRICK_SLAB_BLOCK = register("moonstone_brick_slab", new MoonstoneBrickSlabBlock(MOONSTONE_SETTINGS));

    public static final Block MOONSTONE_BRICK_STAIRS_BLOCK = register("moonstone_brick_stairs", new MoonstoneBrickStairBlock(MOONSTONE_BRICKS_BLOCK.getDefaultState(), MOONSTONE_SETTINGS));

    public static final Block MOONSTONE_BRICK_WALL_BLOCK = register("moonstone_brick_wall", new MoonstoneBrickWallBlock(MOONSTONE_SETTINGS));

    // -- Moonwell
    // TODO: Figure out how to make this block get ignored by raycasting
    public static final Block MOONWELL_FAKE_FLUID_BLOCK = register("moonwell_fake_fluid", new MoonwellFakeFluidBlock(
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
                    .luminance(state -> 3)
                    .air()
            ));

    public static final Block CHISELED_MOONWELL_BRICKS_FULL_MOON_BLOCK = register("chiseled_moonwell_bricks_full_moon", new ChiseledMoonwellBricksBlock(LunarPhasesEnum.FULL_MOON,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .dropsLike(CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK)
                    .requiresTool().
                    strength(2.0F, 6.0F)));

    public static final Block CHISELED_MOONWELL_BRICKS_WANING_GIBBOUS_BLOCK = register("chiseled_moonwell_bricks_waning_gibbous", new ChiseledMoonwellBricksBlock(LunarPhasesEnum.WANING_GIBBOUS,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .dropsLike(CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK)
                    .requiresTool().
                    strength(2.0F, 6.0F)));

    public static final Block CHISELED_MOONWELL_BRICKS_THIRD_QUARTER_BLOCK = register("chiseled_moonwell_bricks_third_quarter", new ChiseledMoonwellBricksBlock(LunarPhasesEnum.THIRD_QUARTER,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .dropsLike(CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK)
                    .requiresTool().
                    strength(2.0F, 6.0F)));

    public static final Block CHISELED_MOONWELL_BRICKS_WANING_CRESCENT_BLOCK = register("chiseled_moonwell_bricks_waning_crescent", new ChiseledMoonwellBricksBlock(LunarPhasesEnum.WANING_CRESCENT,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .dropsLike(CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK)
                    .requiresTool().
                    strength(2.0F, 6.0F)));

    public static final Block CHISELED_MOONWELL_BRICKS_NEW_MOON_BLOCK = register("chiseled_moonwell_bricks_new_moon", new ChiseledMoonwellBricksBlock(LunarPhasesEnum.NEW_MOON,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .dropsLike(CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK)
                    .requiresTool().
                    strength(2.0F, 6.0F)));

    public static final Block CHISELED_MOONWELL_BRICKS_WAXING_CRESCENT_BLOCK = register("chiseled_moonwell_bricks_waxing_crescent", new ChiseledMoonwellBricksBlock(LunarPhasesEnum.WAXING_CRESCENT,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .dropsLike(CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK)
                    .requiresTool().
                    strength(2.0F, 6.0F)));

    public static final Block CHISELED_MOONWELL_BRICKS_FIRST_QUARTER_BLOCK = register("chiseled_moonwell_bricks_first_quarter", new ChiseledMoonwellBricksBlock(LunarPhasesEnum.FIRST_QUARTER,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .dropsLike(CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK)
                    .requiresTool().
                    strength(2.0F, 6.0F)));

    public static final Block CHISELED_MOONWELL_BRICKS_WAXING_GIBBOUS_BLOCK = register("chiseled_moonwell_bricks_waxing_gibbous", new ChiseledMoonwellBricksBlock(LunarPhasesEnum.WANING_GIBBOUS,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .dropsLike(CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK)
                    .requiresTool().
                    strength(2.0F, 6.0F)));

    public static final Block CRACKED_MOONWELL_BRICKS_BLOCK = register("cracked_moonwell_bricks", new MoonwellBrickBlock(
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .dropsLike(CRACKED_MOONSTONE_BRICKS_BLOCK)
                    .requiresTool().
                    strength(2.0F, 6.0F)));

    public static final Block MOONWELL_BASIN_BLOCK = register("moonwell_basin", new MoonwellBasinBlock(
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .dropsLike(Blocks.CAULDRON)
                    .requiresTool()
                    .strength(2.0F, 6.0F)));

    public static final Block MOONWELL_BRICKS_BLOCK = register("moonwell_bricks", new MoonwellBrickBlock(
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .dropsLike(MOONSTONE_BRICKS_BLOCK)
                    .requiresTool().
                    strength(2.0F, 6.0F)));

    public static final Block MOONWELL_BRICK_SLAB_BLOCK = register("moonwell_brick_slab", new MoonwellBrickSlabBlock(
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .dropsLike(MOONSTONE_BRICK_SLAB_BLOCK)
                    .requiresTool().
                    strength(2.0F, 6.0F)));

    public static final Block MOONWELL_BRICK_STAIRS_BLOCK = register("moonwell_brick_stairs",
            new MoonwellBrickStairBlock(MOONWELL_BRICKS_BLOCK.getDefaultState(),
                    AbstractBlock.Settings.create()
                        .mapColor(MapColor.LIGHT_BLUE)
                        .instrument(NoteBlockInstrument.BASEDRUM)
                        .dropsLike(MOONSTONE_BRICK_STAIRS_BLOCK)
                        .requiresTool().
                        strength(2.0F, 6.0F)));

    public static final Block MOONWELL_BRICK_WALL_BLOCK = register("moonwell_brick_wall", new MoonwellBrickWallBlock(
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .dropsLike(MOONSTONE_BRICK_WALL_BLOCK)
                    .requiresTool().
                    strength(2.0F, 6.0F)));


    // -- Waxed Moonstone
    public static final Block WAXED_CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK = register("waxed_chiseled_moonstone_bricks_full_moon", new WaxedChiseledMoonstoneBricksBlock(LunarPhasesEnum.FULL_MOON,MOONSTONE_SETTINGS));

    public static final Block WAXED_CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK = register("waxed_chiseled_moonstone_bricks_waning_gibbous", new WaxedChiseledMoonstoneBricksBlock(LunarPhasesEnum.WANING_GIBBOUS,MOONSTONE_SETTINGS));

    public static final Block WAXED_CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK = register("waxed_chiseled_moonstone_bricks_third_quarter", new WaxedChiseledMoonstoneBricksBlock(LunarPhasesEnum.THIRD_QUARTER,MOONSTONE_SETTINGS));

    public static final Block WAXED_CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK = register("waxed_chiseled_moonstone_bricks_waning_crescent", new WaxedChiseledMoonstoneBricksBlock(LunarPhasesEnum.WANING_CRESCENT,MOONSTONE_SETTINGS));

    public static final Block WAXED_CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK = register("waxed_chiseled_moonstone_bricks_new_moon", new WaxedChiseledMoonstoneBricksBlock(LunarPhasesEnum.NEW_MOON,MOONSTONE_SETTINGS));

    public static final Block WAXED_CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK = register("waxed_chiseled_moonstone_bricks_waxing_crescent", new WaxedChiseledMoonstoneBricksBlock(LunarPhasesEnum.WAXING_CRESCENT,MOONSTONE_SETTINGS));

    public static final Block WAXED_CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK = register("waxed_chiseled_moonstone_bricks_first_quarter", new WaxedChiseledMoonstoneBricksBlock(LunarPhasesEnum.FULL_MOON,MOONSTONE_SETTINGS));

    public static final Block WAXED_CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK = register("waxed_chiseled_moonstone_bricks_waxing_gibbous", new WaxedChiseledMoonstoneBricksBlock(LunarPhasesEnum.WAXING_GIBBOUS,MOONSTONE_SETTINGS));

    public static final Block WAXED_CRACKED_MOONSTONE_BRICKS_BLOCK = register("waxed_cracked_moonstone_bricks", new Block(MOONSTONE_SETTINGS));

    public static final Block WAXED_MOONSTONE_BRICKS_BLOCK = register("waxed_moonstone_bricks", new Block(MOONSTONE_SETTINGS));

    public static final Block WAXED_MOONSTONE_BRICK_SLAB_BLOCK = register("waxed_moonstone_brick_slab", new SlabBlock(MOONSTONE_SETTINGS));

    public static final Block WAXED_MOONSTONE_BRICK_STAIRS_BLOCK = register("waxed_moonstone_brick_stairs", new StairsBlock(WAXED_MOONSTONE_BRICKS_BLOCK.getDefaultState(),MOONSTONE_SETTINGS));

    public static final Block WAXED_MOONSTONE_BRICK_WALL_BLOCK = register("waxed_moonstone_brick_wall", new WallBlock(MOONSTONE_SETTINGS));

    // BlockItems
    public static final Item BLESSED_MOON_WATER_ITEM = register("blessed_moon_water",
            new BlockItem(BLESSED_MOON_WATER_BLOCK, new Item.Settings()));

    public static final Item CHISELED_MOONSTONE_BRICKS_FULL_MOON_ITEM = register("chiseled_moonstone_bricks_full_moon",
            new BlockItem(CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK, new Item.Settings()));

    public static final Item CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_ITEM = register("chiseled_moonstone_bricks_waning_gibbous",
            new BlockItem(CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK, new Item.Settings()));

    public static final Item CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_ITEM = register("chiseled_moonstone_bricks_third_quarter",
            new BlockItem(CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK, new Item.Settings()));

    public static final Item CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_ITEM = register("chiseled_moonstone_bricks_waning_crescent",
            new BlockItem(CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK, new Item.Settings()));

    public static final Item CHISELED_MOONSTONE_BRICKS_NEW_MOON_ITEM = register("chiseled_moonstone_bricks_new_moon",
            new BlockItem(CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK, new Item.Settings()));

    public static final Item CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_ITEM = register("chiseled_moonstone_bricks_waxing_crescent",
            new BlockItem(CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK, new Item.Settings()));

    public static final Item CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_ITEM = register("chiseled_moonstone_bricks_first_quarter",
            new BlockItem(CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK, new Item.Settings()));

    public static final Item CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_ITEM = register("chiseled_moonstone_bricks_waxing_gibbous",
            new BlockItem(CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK, new Item.Settings()));

    public static final Item CRACKED_MOONSTONE_BRICKS_ITEM = register("cracked_moonstone_bricks",
            new BlockItem(CRACKED_MOONSTONE_BRICKS_BLOCK, new Item.Settings()));

    public static final Item MOONSTONE_BRICKS_ITEM = register("moonstone_bricks",
            new BlockItem(MOONSTONE_BRICKS_BLOCK, new Item.Settings()));

    public static final Item MOONSTONE_BRICK_SLAB_ITEM = register("moonstone_brick_slab",
            new BlockItem(MOONSTONE_BRICK_SLAB_BLOCK, new Item.Settings()));

    public static final Item MOONSTONE_BRICK_STAIRS_ITEM = register("moonstone_brick_stairs",
            new BlockItem(MOONSTONE_BRICK_STAIRS_BLOCK, new Item.Settings()));

    public static final Item MOONSTONE_BRICK_WALL_ITEM = register("moonstone_brick_wall",
            new BlockItem(MOONSTONE_BRICK_WALL_BLOCK, new Item.Settings()));

    public static final Item MOONWELL_BRICKS_ITEM = register("moonwell_bricks",
            new BlockItem(MOONWELL_BRICKS_BLOCK, new Item.Settings()));

    public static final Item MOONWELL_BRICK_SLAB_ITEM = register("moonwell_brick_slab",
            new BlockItem(MOONWELL_BRICK_SLAB_BLOCK, new Item.Settings()));

    public static final Item MOONWELL_BRICK_STAIRS_ITEM = register("moonwell_brick_stairs",
            new BlockItem(MOONWELL_BRICK_STAIRS_BLOCK, new Item.Settings()));

    public static final Item MOONWELL_BRICK_WALL_ITEM = register("moonwell_brick_wall",
            new BlockItem(MOONWELL_BRICK_WALL_BLOCK, new Item.Settings()));

    public static final Item WAXED_CHISELED_MOONSTONE_BRICKS_FULL_MOON_ITEM = register("waxed_chiseled_moonstone_bricks_full_moon",
            new BlockItem(WAXED_CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK, new Item.Settings()));

    public static final Item WAXED_CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_ITEM = register("waxed_chiseled_moonstone_bricks_waning_gibbous",
            new BlockItem(WAXED_CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK, new Item.Settings()));

    public static final Item WAXED_CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_ITEM = register("waxed_chiseled_moonstone_bricks_third_quarter",
            new BlockItem(WAXED_CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK, new Item.Settings()));

    public static final Item WAXED_CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_ITEM = register("waxed_chiseled_moonstone_bricks_waning_crescent",
            new BlockItem(WAXED_CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK, new Item.Settings()));

    public static final Item WAXED_CHISELED_MOONSTONE_BRICKS_NEW_MOON_ITEM = register("waxed_chiseled_moonstone_bricks_new_moon",
            new BlockItem(WAXED_CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK, new Item.Settings()));

    public static final Item WAXED_CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_ITEM = register("waxed_chiseled_moonstone_bricks_waxing_crescent",
            new BlockItem(WAXED_CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK, new Item.Settings()));

    public static final Item WAXED_CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_ITEM = register("waxed_chiseled_moonstone_bricks_first_quarter",
            new BlockItem(WAXED_CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK, new Item.Settings()));

    public static final Item WAXED_CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_ITEM = register("waxed_chiseled_moonstone_bricks_waxing_gibbous",
            new BlockItem(WAXED_CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK, new Item.Settings()));

    public static final Item WAXED_CRACKED_MOONSTONE_BRICKS_ITEM = register("waxed_cracked_moonstone_bricks",
            new BlockItem(WAXED_CRACKED_MOONSTONE_BRICKS_BLOCK, new Item.Settings()));

    public static final Item WAXED_MOONSTONE_BRICKS_ITEM = register("waxed_moonstone_bricks",
            new BlockItem(WAXED_MOONSTONE_BRICKS_BLOCK, new Item.Settings()));

    public static final Item WAXED_MOONSTONE_BRICK_SLAB_ITEM = register("waxed_moonstone_brick_slab",
            new BlockItem(WAXED_MOONSTONE_BRICK_SLAB_BLOCK, new Item.Settings()));

    public static final Item WAXED_MOONSTONE_BRICK_STAIRS_ITEM = register("waxed_moonstone_brick_stairs",
            new BlockItem(WAXED_MOONSTONE_BRICK_STAIRS_BLOCK, new Item.Settings()));

    public static final Item WAXED_MOONSTONE_BRICK_WALL_ITEM = register("waxed_moonstone_brick_wall",
            new BlockItem(WAXED_MOONSTONE_BRICK_WALL_BLOCK, new Item.Settings()));

    // Items
    public static final Item BLESSED_MOON_WATER_BUCKET_ITEM = register("blessed_moon_water_bucket", new BucketItem(BLESSED_MOON_WATER_FLUID, new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1)));

    public static final Item MOON_PHIAL_ITEM = register("moon_phial",
            new MoonPhialItem(new Item.Settings().maxCount(16).rarity(Rarity.RARE)));


    // Block Entities
    public static final BlockEntityType<MoonwellMultiblockMasterBlockEntity> MOONWELL_MULTIBLOCK_MASTER_BLOCK_ENTITY = register("moonwell_master",
            BlockEntityType.Builder.create(MoonwellMultiblockMasterBlockEntity::new, MOONWELL_BASIN_BLOCK)
                    .build());

    public static final BlockEntityType<MoonwellMultiblockSlaveBlockEntity> MOONWELL_MULTIBLOCK_SLAVE_BLOCK_ENTITY = register("moonwell_slave",
            BlockEntityType.Builder.create(MoonwellMultiblockSlaveBlockEntity::new,
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
            BlockEntityType.Builder.create(MoonwellFakeFluidBlockEntity::new,
                            MOONWELL_FAKE_FLUID_BLOCK)
                    .build());

    // Sound Events
    public static final SoundEvent MOONWELL_ACTIVATE_SOUND = register("moonwell_activate");
    public static final SoundEvent MOONWELL_DEACTIVATE_SOUND = register("moonwell_deactivate");

    // Tags
    // -- Block
    /** Valid blocks allowed in the formation of a {@code Moonwell} **/
    public static final TagKey<Block> MOONSTONE_BLOCKS = registerBlockTag("moonstone_blocks");

    /** Valid blocks that compose a {@code Moonwell} **/
    public static final TagKey<Block> MOONWELL_BLOCKS = registerBlockTag("moonwell_blocks");

    /** Valid fluids considered {@code Blessed Moon Water} **/
    public static final TagKey<Fluid> BLESSED_MOON_WATERS = registerFluidTag("blessed_moon_waters");

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
            MOON_PHIAL_ITEM);

    // Registration Functions
    public static <T extends FlowableFluid> T register(String name, T fluid) {
        return Registry.register(Registries.FLUID, Groves.id(name), fluid);
    }

    public static <T extends Block> T register(String name, T block) {
        return Registry.register(Registries.BLOCK, Groves.id(name), block);
    }

    public static <T extends Block> T registerWithItem(String name, T block, Item.Settings settings) {
        T registered = register(name, block);
        register(name, new BlockItem(registered, settings));
        return registered;
    }

    public static <T extends Block> T registerWithItem(String name, T block) {
        return registerWithItem(name, block, new Item.Settings());
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



    }
}
