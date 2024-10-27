package github.xevira.groves.data.provider;

import github.xevira.groves.Registration;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        // Minecraft block tags
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK)
                .add(Registration.CRACKED_MOONSTONE_BRICKS_BLOCK)
                .add(Registration.MOONSTONE_BRICKS_BLOCK)
                .add(Registration.MOONSTONE_BRICK_SLAB_BLOCK)
                .add(Registration.MOONSTONE_BRICK_STAIRS_BLOCK)
                .add(Registration.MOONSTONE_BRICK_WALL_BLOCK)
                .add(Registration.CHISELED_MOONWELL_BRICKS_FULL_MOON_BLOCK)
                .add(Registration.CHISELED_MOONWELL_BRICKS_WANING_GIBBOUS_BLOCK)
                .add(Registration.CHISELED_MOONWELL_BRICKS_THIRD_QUARTER_BLOCK)
                .add(Registration.CHISELED_MOONWELL_BRICKS_WANING_CRESCENT_BLOCK)
                .add(Registration.CHISELED_MOONWELL_BRICKS_NEW_MOON_BLOCK)
                .add(Registration.CHISELED_MOONWELL_BRICKS_WAXING_CRESCENT_BLOCK)
                .add(Registration.CHISELED_MOONWELL_BRICKS_FIRST_QUARTER_BLOCK)
                .add(Registration.CHISELED_MOONWELL_BRICKS_WAXING_GIBBOUS_BLOCK)
                .add(Registration.CRACKED_MOONWELL_BRICKS_BLOCK)
                .add(Registration.MOONWELL_BASIN_BLOCK)
                .add(Registration.MOONWELL_BRICKS_BLOCK)
                .add(Registration.MOONWELL_BRICK_SLAB_BLOCK)
                .add(Registration.MOONWELL_BRICK_STAIRS_BLOCK)
                .add(Registration.MOONWELL_BRICK_WALL_BLOCK)
                .add(Registration.WAXED_CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK)
                .add(Registration.WAXED_CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK)
                .add(Registration.WAXED_CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK)
                .add(Registration.WAXED_CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK)
                .add(Registration.WAXED_CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK)
                .add(Registration.WAXED_CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK)
                .add(Registration.WAXED_CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK)
                .add(Registration.WAXED_CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK)
                .add(Registration.WAXED_CRACKED_MOONSTONE_BRICKS_BLOCK)
                .add(Registration.WAXED_MOONSTONE_BRICKS_BLOCK)
                .add(Registration.WAXED_MOONSTONE_BRICK_SLAB_BLOCK)
                .add(Registration.WAXED_MOONSTONE_BRICK_STAIRS_BLOCK)
                .add(Registration.WAXED_MOONSTONE_BRICK_WALL_BLOCK)
        ;

        getOrCreateTagBuilder(BlockTags.SLABS)
                .add(Registration.MOONSTONE_BRICK_SLAB_BLOCK)
                .add(Registration.MOONWELL_BRICK_SLAB_BLOCK)
                .add(Registration.WAXED_MOONSTONE_BRICK_SLAB_BLOCK)
        ;

        getOrCreateTagBuilder(BlockTags.STAIRS)
                .add(Registration.MOONSTONE_BRICK_STAIRS_BLOCK)
                .add(Registration.MOONWELL_BRICK_STAIRS_BLOCK)
                .add(Registration.WAXED_MOONSTONE_BRICK_STAIRS_BLOCK)
        ;

        getOrCreateTagBuilder(BlockTags.WALLS)
                .add(Registration.MOONSTONE_BRICK_WALL_BLOCK)
                .add(Registration.MOONWELL_BRICK_WALL_BLOCK)
                .add(Registration.WAXED_MOONSTONE_BRICK_WALL_BLOCK)
        ;

        // Mod block tags
        getOrCreateTagBuilder(Registration.MOONSTONE_BLOCKS)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK)
                .add(Registration.CRACKED_MOONSTONE_BRICKS_BLOCK)
                .add(Registration.MOONSTONE_BRICKS_BLOCK)
                .add(Registration.MOONSTONE_BRICK_SLAB_BLOCK)
                .add(Registration.MOONSTONE_BRICK_STAIRS_BLOCK)
                .add(Registration.MOONSTONE_BRICK_WALL_BLOCK);

        getOrCreateTagBuilder(Registration.MOONWELL_BLOCKS)
                .add(Registration.MOONWELL_BASIN_BLOCK)
                .add(Registration.CHISELED_MOONWELL_BRICKS_FULL_MOON_BLOCK)
                .add(Registration.CHISELED_MOONWELL_BRICKS_WANING_GIBBOUS_BLOCK)
                .add(Registration.CHISELED_MOONWELL_BRICKS_THIRD_QUARTER_BLOCK)
                .add(Registration.CHISELED_MOONWELL_BRICKS_WANING_CRESCENT_BLOCK)
                .add(Registration.CHISELED_MOONWELL_BRICKS_NEW_MOON_BLOCK)
                .add(Registration.CHISELED_MOONWELL_BRICKS_WAXING_CRESCENT_BLOCK)
                .add(Registration.CHISELED_MOONWELL_BRICKS_FIRST_QUARTER_BLOCK)
                .add(Registration.CHISELED_MOONWELL_BRICKS_WAXING_GIBBOUS_BLOCK)
                .add(Registration.CRACKED_MOONWELL_BRICKS_BLOCK)
                .add(Registration.MOONWELL_BRICKS_BLOCK)
                .add(Registration.MOONWELL_BRICK_SLAB_BLOCK)
                .add(Registration.MOONWELL_BRICK_STAIRS_BLOCK)
                .add(Registration.MOONWELL_BRICK_WALL_BLOCK);

        getOrCreateTagBuilder(Registration.MOONWELL_INTERACTION_BLOCKS)
                .addTag(Registration.MOONWELL_BLOCKS)
                .add(Registration.MOONWELL_FAKE_FLUID_BLOCK);
    }
}
