package github.xevira.groves.data.provider;

import github.xevira.groves.Registration;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModBlockLootTableProvider extends FabricBlockLootTableProvider {
    public ModBlockLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        addDrop(Registration.CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK);
        addDrop(Registration.CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK);
        addDrop(Registration.CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK);
        addDrop(Registration.CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK);
        addDrop(Registration.CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK);
        addDrop(Registration.CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK);
        addDrop(Registration.CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK);
        addDrop(Registration.CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK);
        addDrop(Registration.CRACKED_MOONSTONE_BRICKS_BLOCK);
        addDrop(Registration.MOONSTONE_BRICKS_BLOCK);
        addDrop(Registration.MOONSTONE_BRICK_SLAB_BLOCK);
        addDrop(Registration.MOONSTONE_BRICK_STAIRS_BLOCK);
        addDrop(Registration.MOONSTONE_BRICK_WALL_BLOCK);

        addDrop(Registration.WAXED_CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK);
        addDrop(Registration.WAXED_CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK);
        addDrop(Registration.WAXED_CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK);
        addDrop(Registration.WAXED_CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK);
        addDrop(Registration.WAXED_CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK);
        addDrop(Registration.WAXED_CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK);
        addDrop(Registration.WAXED_CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK);
        addDrop(Registration.WAXED_CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK);
        addDrop(Registration.WAXED_CRACKED_MOONSTONE_BRICKS_BLOCK);
        addDrop(Registration.WAXED_MOONSTONE_BRICKS_BLOCK);
        addDrop(Registration.WAXED_MOONSTONE_BRICK_SLAB_BLOCK);
        addDrop(Registration.WAXED_MOONSTONE_BRICK_STAIRS_BLOCK);
        addDrop(Registration.WAXED_MOONSTONE_BRICK_WALL_BLOCK);
    }
}
