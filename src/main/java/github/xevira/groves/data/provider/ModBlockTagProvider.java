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
                .add(Registration.AQUAMARINE_BLOCK_BLOCK)
                .add(Registration.AQUAMARINE_ORE_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK)
                .add(Registration.CRACKED_MOONSTONE_BRICKS_BLOCK)
                .add(Registration.DEEPSLATE_AQUAMARINE_ORE_BLOCK)
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

        getOrCreateTagBuilder(BlockTags.AXE_MINEABLE)
                .add(Registration.SANCTUM_LOG_BLOCK)
                .add(Registration.SANCTUM_WOOD_BLOCK)
                .add(Registration.STRIPPED_SANCTUM_LOG_BLOCK)
                .add(Registration.STRIPPED_SANCTUM_WOOD_BLOCK)
                .add(Registration.SANCTUM_CORE_LOG_BLOCK)
                ;

        getOrCreateTagBuilder(BlockTags.NEEDS_STONE_TOOL)
                .add(Registration.SANCTUM_CORE_LOG_BLOCK)
                ;


        getOrCreateTagBuilder(BlockTags.NEEDS_IRON_TOOL)
                .add(Registration.AQUAMARINE_BLOCK_BLOCK)
                .add(Registration.AQUAMARINE_ORE_BLOCK)
                .add(Registration.DEEPSLATE_AQUAMARINE_ORE_BLOCK);

        getOrCreateTagBuilder(BlockTags.BEACON_BASE_BLOCKS)
                .add(Registration.AQUAMARINE_BLOCK_BLOCK);

        getOrCreateTagBuilder(BlockTags.LEAVES)
                .add(Registration.SANCTUM_LEAVES_BLOCK)
                ;

        getOrCreateTagBuilder(BlockTags.LOGS_THAT_BURN)
                .addTag(Registration.SANCTUM_LOG_BLOCKS)
                ;

        getOrCreateTagBuilder(BlockTags.WOODEN_BUTTONS)
                .add(Registration.SANCTUM_BUTTON_BLOCK);

        getOrCreateTagBuilder(BlockTags.WOODEN_DOORS)
                .add(Registration.SANCTUM_DOOR_BLOCK);

        getOrCreateTagBuilder(BlockTags.WOODEN_TRAPDOORS)
                .add(Registration.SANCTUM_TRAPDOOR_BLOCK);

        getOrCreateTagBuilder(BlockTags.WOODEN_FENCES)
                .add(Registration.SANCTUM_FENCE_BLOCK);

        getOrCreateTagBuilder(BlockTags.WOODEN_PRESSURE_PLATES)
                .add(Registration.SANCTUM_PRESSURE_PLATE_BLOCK);

        getOrCreateTagBuilder(BlockTags.WOODEN_SLABS)
                .add(Registration.SANCTUM_SLAB_BLOCK);

        getOrCreateTagBuilder(BlockTags.WOODEN_STAIRS)
                .add(Registration.SANCTUM_STAIRS_BLOCK);

        getOrCreateTagBuilder(BlockTags.STANDING_SIGNS)
                .add(Registration.SANCTUM_SIGN_BLOCK);

        getOrCreateTagBuilder(BlockTags.WALL_SIGNS)
                .add(Registration.SANCTUM_WALL_SIGN_BLOCK);

        getOrCreateTagBuilder(BlockTags.CEILING_HANGING_SIGNS)
                .add(Registration.SANCTUM_HANGING_SIGN_BLOCK);

        getOrCreateTagBuilder(BlockTags.WALL_HANGING_SIGNS)
                .add(Registration.SANCTUM_WALL_HANGING_SIGN_BLOCK);

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

        getOrCreateTagBuilder(Registration.MOONWELL_CONSTRUCTION_BLOCKS)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK)
                .add(Registration.CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK)
                .add(Registration.CRACKED_MOONSTONE_BRICKS_BLOCK)
                .add(Registration.MOONSTONE_BRICKS_BLOCK);

        getOrCreateTagBuilder(Registration.SANCTUM_LOG_BLOCKS)
                .add(Registration.SANCTUM_LOG_BLOCK)
                .add(Registration.SANCTUM_WOOD_BLOCK)
                .add(Registration.STRIPPED_SANCTUM_LOG_BLOCK)
                .add(Registration.STRIPPED_SANCTUM_WOOD_BLOCK)
                ;
    }
}
