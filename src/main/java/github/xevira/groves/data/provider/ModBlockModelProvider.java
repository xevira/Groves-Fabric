package github.xevira.groves.data.provider;

import github.xevira.groves.Registration;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.data.client.*;
import net.minecraft.util.Identifier;

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
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(Registration.BLESSED_MOON_WATER_BUCKET_ITEM, Models.HANDHELD);
        itemModelGenerator.register(Registration.IMPRINTING_SIGIL_ITEM, Models.HANDHELD);
        itemModelGenerator.register(Registration.ENCHANTED_IMPRINTING_SIGIL_ITEM, Registration.IMPRINTING_SIGIL_ITEM, Models.HANDHELD);
        itemModelGenerator.register(Registration.MOONLIGHT_BUCKET_ITEM, Models.HANDHELD);
    }
}