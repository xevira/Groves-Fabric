package github.xevira.groves.data.provider;

import github.xevira.groves.Registration;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModBlockLootTableProvider extends FabricBlockLootTableProvider {
    public ModBlockLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    public LootTable.Builder fortuneOreDrops(Block block, Item item, float min, float max) {
        RegistryWrapper.Impl<Enchantment> impl = this.registries.getOrThrow(RegistryKeys.ENCHANTMENT);
        return this.dropsWithSilkTouch(
                block,
                (LootPoolEntry.Builder<?>)this.applyExplosionDecay(
                        block,
                        ItemEntry.builder(item)
                                .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(min, max)))
                                .apply(ApplyBonusLootFunction.uniformBonusCount(impl.getOrThrow(Enchantments.FORTUNE)))
                )
        );
    }


    @Override
    public void generate() {
        addDrop(Registration.AQUAMARINE_ORE_BLOCK, block -> oreDrops(block, Registration.AQUAMARINE_ITEM));
        addDrop(Registration.CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK);
        addDrop(Registration.CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK);
        addDrop(Registration.CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK);
        addDrop(Registration.CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK);
        addDrop(Registration.CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK);
        addDrop(Registration.CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK);
        addDrop(Registration.CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK);
        addDrop(Registration.CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK);
        addDrop(Registration.CRACKED_MOONSTONE_BRICKS_BLOCK);
        addDrop(Registration.DEEPSLATE_AQUAMARINE_ORE_BLOCK, block -> oreDrops(block, Registration.AQUAMARINE_ITEM));
        addDrop(Registration.MOONSTONE_BRICKS_BLOCK);
        addDrop(Registration.MOONSTONE_BRICK_SLAB_BLOCK);
        addDrop(Registration.MOONSTONE_BRICK_STAIRS_BLOCK);
        addDrop(Registration.MOONSTONE_BRICK_WALL_BLOCK);

        addDrop(Registration.SANCTUM_LOG_BLOCK);
        addDrop(Registration.SANCTUM_WOOD_BLOCK);
        addDrop(Registration.STRIPPED_SANCTUM_LOG_BLOCK);
        addDrop(Registration.STRIPPED_SANCTUM_WOOD_BLOCK);
        addDrop(Registration.SANCTUM_CORE_LOG_BLOCK, block -> fortuneOreDrops(block, Registration.IRONWOOD_SHARD_ITEM, 1.0f, 3.0f));

        leavesDrops(Registration.SANCTUM_LEAVES_BLOCK, Registration.SANCTUM_SAPLING_BLOCK, SAPLING_DROP_CHANCE);
        addDrop(Registration.SANCTUM_SAPLING_BLOCK);

        addDrop(Registration.SANCTUM_PLANKS_BLOCK);
        slabDrops(Registration.SANCTUM_SLAB_BLOCK);
        addDrop(Registration.SANCTUM_STAIRS_BLOCK);
        doorDrops(Registration.SANCTUM_DOOR_BLOCK);
        addDrop(Registration.SANCTUM_TRAPDOOR_BLOCK);
        addDrop(Registration.SANCTUM_BUTTON_BLOCK);
        addDrop(Registration.SANCTUM_PRESSURE_PLATE_BLOCK);
        addDrop(Registration.SANCTUM_FENCE_BLOCK);
        addDrop(Registration.SANCTUM_FENCE_GATE_BLOCK);
        addDrop(Registration.SANCTUM_SIGN_BLOCK);
        addDrop(Registration.SANCTUM_HANGING_SIGN_BLOCK);

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
