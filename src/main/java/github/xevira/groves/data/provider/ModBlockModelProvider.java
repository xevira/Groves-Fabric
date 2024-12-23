package github.xevira.groves.data.provider;

import github.xevira.groves.Registration;
import github.xevira.groves.client.item.MoonPhaseProperty;
import github.xevira.groves.item.UnlockScrollItem;
import github.xevira.groves.sanctuary.GroveAbilities;
import github.xevira.groves.sanctuary.GroveAbility;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.client.data.*;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.RangeDispatchItemModel;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ModBlockModelProvider extends FabricModelProvider {
    public ModBlockModelProvider(FabricDataOutput output) {
        super(output);
    }

    private void registerBrickSlabs(BlockStateModelGenerator blockStateModelGenerator, Block block, Block slab)
    {
        TextureMap textureMap = TextureMap.all(block);
        TextureMap textureMap2 = TextureMap.sideEnd(TextureMap.getId(block), textureMap.getTexture(TextureKey.TOP));
        Identifier identifier = Models.SLAB.upload(slab, textureMap2, blockStateModelGenerator.modelCollector);
        Identifier identifier2 = Models.SLAB_TOP.upload(slab, textureMap2, blockStateModelGenerator.modelCollector);
        Identifier identifier3 = Models.CUBE_ALL.upload(block, textureMap, blockStateModelGenerator.modelCollector);

        blockStateModelGenerator.blockStateCollector.accept(BlockStateModelGenerator.createSlabBlockState(slab, identifier, identifier2, identifier3));
        blockStateModelGenerator.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(block, identifier3));
    }

    private BlockStateModelGenerator.BlockTexturePool registerCubeAllModelTexturePoolWithVisual(BlockStateModelGenerator blockStateModelGenerator, Block block, Block visual)
    {
        TexturedModel texturedModel = TexturedModel.CUBE_ALL.get(visual);
        return blockStateModelGenerator.new BlockTexturePool(texturedModel.getTextures()).base(block, texturedModel.getModel());
    }

    private void registerBrickFamily(BlockStateModelGenerator blockStateModelGenerator, Block baseBlock, Block slab, Block stairs, Block wall)
    {
        BlockStateModelGenerator.BlockTexturePool pool = blockStateModelGenerator.registerCubeAllModelTexturePool(baseBlock);

        pool.slab(slab);
        pool.stairs(stairs);
        pool.wall(wall);

        // Anything else?
    }


    private void registerBrickFamilyWithVisual(BlockStateModelGenerator blockStateModelGenerator, Block visual, Block baseBlock, Block slab, Block stairs, Block wall)
    {
        BlockStateModelGenerator.BlockTexturePool pool = registerCubeAllModelTexturePoolWithVisual(blockStateModelGenerator, baseBlock, visual);

        pool.slab(slab);
        pool.stairs(stairs);
        pool.wall(wall);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        registerBrickFamily(blockStateModelGenerator,
                Registration.MOONSTONE_BRICKS_BLOCK,
                Registration.MOONSTONE_BRICK_SLAB_BLOCK,
                Registration.MOONSTONE_BRICK_STAIRS_BLOCK,
                Registration.MOONSTONE_BRICK_WALL_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(Registration.CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(Registration.CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(Registration.CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(Registration.CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(Registration.CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(Registration.CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(Registration.CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(Registration.CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(Registration.CRACKED_MOONSTONE_BRICKS_BLOCK);


        blockStateModelGenerator.registerSimpleState(Registration.MOONWELL_BASIN_BLOCK);
        registerBrickFamily(blockStateModelGenerator,
                Registration.MOONWELL_BRICKS_BLOCK,
                Registration.MOONWELL_BRICK_SLAB_BLOCK,
                Registration.MOONWELL_BRICK_STAIRS_BLOCK,
                Registration.MOONWELL_BRICK_WALL_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(Registration.CHISELED_MOONWELL_BRICKS_FULL_MOON_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(Registration.CHISELED_MOONWELL_BRICKS_WANING_GIBBOUS_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(Registration.CHISELED_MOONWELL_BRICKS_THIRD_QUARTER_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(Registration.CHISELED_MOONWELL_BRICKS_WANING_CRESCENT_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(Registration.CHISELED_MOONWELL_BRICKS_NEW_MOON_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(Registration.CHISELED_MOONWELL_BRICKS_WAXING_CRESCENT_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(Registration.CHISELED_MOONWELL_BRICKS_FIRST_QUARTER_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(Registration.CHISELED_MOONWELL_BRICKS_WAXING_GIBBOUS_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(Registration.CRACKED_MOONWELL_BRICKS_BLOCK);


        registerBrickFamilyWithVisual(blockStateModelGenerator,
                Registration.MOONSTONE_BRICKS_BLOCK,
                Registration.WAXED_MOONSTONE_BRICKS_BLOCK,
                Registration.WAXED_MOONSTONE_BRICK_SLAB_BLOCK,
                Registration.WAXED_MOONSTONE_BRICK_STAIRS_BLOCK,
                Registration.WAXED_MOONSTONE_BRICK_WALL_BLOCK);

        blockStateModelGenerator.registerParented(Registration.CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK, Registration.WAXED_CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK);
        blockStateModelGenerator.registerParented(Registration.CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK, Registration.WAXED_CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK);
        blockStateModelGenerator.registerParented(Registration.CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK, Registration.WAXED_CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK);
        blockStateModelGenerator.registerParented(Registration.CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK, Registration.WAXED_CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK);
        blockStateModelGenerator.registerParented(Registration.CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK, Registration.WAXED_CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK);
        blockStateModelGenerator.registerParented(Registration.CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK, Registration.WAXED_CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK);
        blockStateModelGenerator.registerParented(Registration.CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK, Registration.WAXED_CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK);
        blockStateModelGenerator.registerParented(Registration.CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK, Registration.WAXED_CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK);
        blockStateModelGenerator.registerParented(Registration.CRACKED_MOONSTONE_BRICKS_BLOCK, Registration.WAXED_CRACKED_MOONSTONE_BRICKS_BLOCK);

        blockStateModelGenerator.registerSimpleState(Registration.BLESSED_MOON_WATER_BLOCK);
        blockStateModelGenerator.registerSimpleState(Registration.MOONLIGHT_BLOCK);

        blockStateModelGenerator.registerSimpleCubeAll(Registration.AQUAMARINE_ORE_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(Registration.DEEPSLATE_AQUAMARINE_ORE_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(Registration.AQUAMARINE_BLOCK_BLOCK);

        blockStateModelGenerator.registerLog(Registration.SANCTUM_LOG_BLOCK).log(Registration.SANCTUM_LOG_BLOCK).wood(Registration.SANCTUM_WOOD_BLOCK);
        blockStateModelGenerator.registerLog(Registration.SANCTUM_CORE_LOG_BLOCK).log(Registration.SANCTUM_CORE_LOG_BLOCK);
        blockStateModelGenerator.registerLog(Registration.STRIPPED_SANCTUM_LOG_BLOCK).log(Registration.STRIPPED_SANCTUM_LOG_BLOCK).wood(Registration.STRIPPED_SANCTUM_WOOD_BLOCK);;
        blockStateModelGenerator.registerSimpleCubeAll(Registration.SANCTUM_LEAVES_BLOCK);
        blockStateModelGenerator.registerFlowerPotPlant(Registration.SANCTUM_SAPLING_BLOCK, Registration.POTTED_SANCTUM_SAPLING_BLOCK, BlockStateModelGenerator.CrossType.NOT_TINTED);

        BlockFamily sanctumFamily = new BlockFamily.Builder(Registration.SANCTUM_PLANKS_BLOCK)
                .button(Registration.SANCTUM_BUTTON_BLOCK)
                .fence(Registration.SANCTUM_FENCE_BLOCK)
                .fenceGate(Registration.SANCTUM_FENCE_GATE_BLOCK)
                .pressurePlate(Registration.SANCTUM_PRESSURE_PLATE_BLOCK)
                .sign(Registration.SANCTUM_SIGN_BLOCK, Registration.SANCTUM_WALL_SIGN_BLOCK)
                .slab(Registration.SANCTUM_SLAB_BLOCK)
                .stairs(Registration.SANCTUM_STAIRS_BLOCK)
                .door(Registration.SANCTUM_DOOR_BLOCK)
                .trapdoor(Registration.SANCTUM_TRAPDOOR_BLOCK)
                .group("wooden")
                .unlockCriterionName("has_planks")
                .build();
        blockStateModelGenerator.registerCubeAllModelTexturePool(sanctumFamily.getBaseBlock())
                        .family(sanctumFamily);

        blockStateModelGenerator.registerHangingSign(Registration.STRIPPED_SANCTUM_LOG_BLOCK, Registration.SANCTUM_HANGING_SIGN_BLOCK, Registration.SANCTUM_WALL_HANGING_SIGN_BLOCK);

        blockStateModelGenerator.registerSimpleState(Registration.WIND_CHIME_BLOCK);
        blockStateModelGenerator.registerSimpleState(Registration.WORN_WIND_CHIME_BLOCK);
        blockStateModelGenerator.registerSimpleState(Registration.DAMAGED_WIND_CHIME_BLOCK);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(Registration.AQUAMARINE_ITEM, Models.HANDHELD);
        itemModelGenerator.register(Registration.AQUAMARINE_DUST_ITEM, Models.HANDHELD);
        itemModelGenerator.register(Registration.BLESSED_MOON_WATER_BUCKET_ITEM, Models.HANDHELD);
        itemModelGenerator.register(Registration.IMPRINTING_SIGIL_ITEM, Models.HANDHELD);

        // TODO: Verify this
        itemModelGenerator.registerWithTextureSource(Registration.ENCHANTED_IMPRINTING_SIGIL_ITEM, Registration.IMPRINTING_SIGIL_ITEM, Models.HANDHELD);
        itemModelGenerator.register(Registration.INTO_THE_HEART_OF_THE_UNIVERSE_MUSIC_DISC_ITEM, Models.GENERATED);
        registerMoonPhial(itemModelGenerator, Registration.MOON_PHIAL_ITEM);
        itemModelGenerator.register(Registration.MOONLIGHT_BUCKET_ITEM, Models.HANDHELD);
        itemModelGenerator.register(Registration.IRONWOOD_SHARD_ITEM, Models.GENERATED);
        itemModelGenerator.register(Registration.UNLOCK_SCROLL_ITEM, Models.HANDHELD);
        itemModelGenerator.register(Registration.FORBIDDEN_SCROLL_ITEM, Models.HANDHELD);
        itemModelGenerator.register(Registration.SANCTUM_BOAT_ITEM, Models.GENERATED);
        itemModelGenerator.register(Registration.SANCTUM_CHEST_BOAT_ITEM, Models.GENERATED);
        itemModelGenerator.register(Registration.SANCTUM_SWORD_ITEM, Models.HANDHELD);
        itemModelGenerator.register(Registration.SANCTUM_PICKAXE_ITEM, Models.HANDHELD);
        itemModelGenerator.register(Registration.SANCTUM_AXE_ITEM, Models.HANDHELD);
        itemModelGenerator.register(Registration.SANCTUM_SHOVEL_ITEM, Models.HANDHELD);
        itemModelGenerator.register(Registration.SANCTUM_HOE_ITEM, Models.HANDHELD);

        itemModelGenerator.register(Registration.ENDER_HEART_ITEM, Models.GENERATED);
        itemModelGenerator.register(Registration.GHAST_HEART_ITEM, Models.GENERATED);
        itemModelGenerator.register(Registration.SHULKER_BULLET_ITEM, Models.GENERATED);
        itemModelGenerator.register(Registration.EAGLE_FEATHER_ITEM, Models.GENERATED);
        itemModelGenerator.register(Registration.DOLPHIN_FIN_ITEM, Models.GENERATED);
        itemModelGenerator.register(Registration.SPIDER_LEG_ITEM, Models.GENERATED);
        itemModelGenerator.register(Registration.POLLEN_ITEM, Models.GENERATED);
        itemModelGenerator.register(Registration.BEE_STINGER_ITEM, Models.GENERATED);

        for(GroveAbility ability : GroveAbilities.ABILITIES.values())
        {
            List<UnlockScrollItem> scrolls = GroveAbilities.UNLOCK_SCROLLS.get(ability.getName());
            if (scrolls != null) {

                scrolls.forEach(scroll -> {
                    if (ability.isForbidden())
                        itemModelGenerator.registerWithTextureSource(scroll, Registration.FORBIDDEN_SCROLL_ITEM, Models.HANDHELD);
                    else
                        itemModelGenerator.registerWithTextureSource(scroll, Registration.UNLOCK_SCROLL_ITEM, Models.HANDHELD);
                });
            }
        }
    }

    private void registerMoonPhial(ItemModelGenerator generator, Item phial)
    {
        List<RangeDispatchItemModel.Entry> list = new ArrayList<>();
        ItemModel.Unbaked unbaked = ItemModels.basic(generator.upload(phial, Models.GENERATED));
        list.add(ItemModels.rangeDispatchEntry(unbaked, 0.0f));

        for (int i = 1; i < 16; i++) {
            ItemModel.Unbaked unbaked2 = ItemModels.basic(generator.registerSubModel(phial, String.format(Locale.ROOT, "_%02d", i), Models.GENERATED));
            list.add(ItemModels.rangeDispatchEntry(unbaked2, (float)i));
        }

        generator.output
                .accept(
                        phial,
                        ItemModels.overworldSelect(
                                ItemModels.rangeDispatch(new MoonPhaseProperty(false), 16.0f, list),
                                ItemModels.rangeDispatch(new MoonPhaseProperty(true), 16.0f, list)
                        )
                );

    }
}
