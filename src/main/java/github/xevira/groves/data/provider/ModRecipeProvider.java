package github.xevira.groves.data.provider;

import github.xevira.groves.Registration;
import github.xevira.groves.util.WaxHelper;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Block;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeGenerator;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup wrapperLookup, RecipeExporter exporter) {
        return new RecipeGenerator(wrapperLookup, exporter) {
            private void generateLunarCycleShapeless(RecipeExporter exporter, RecipeCategory category, List<Block> phases)
            {
                for(int i = 0; i < phases.size(); i++)
                {
                    Block block1 = phases.get(i);
                    Block block2 = phases.get((i + 1) % phases.size());

                    createShapeless(category, block2)
                            .input(block1.asItem())
                            .group(getItemPath(block2))
                            .criterion(hasItem(block1.asItem()), conditionsFromItem(block1.asItem()))
                            .offerTo(exporter, convertBetween(block2, block1));
                }
            }

            private void generateLunarCycleStonecutting(RecipeCategory category, Block input, List<Block> phases)
            {
                phases.forEach(block -> offerStonecuttingRecipe(category, block.asItem(), input.asItem()));
            }


            private void generateLunarPhaseWaxing(RecipeExporter exporter, RecipeCategory category, List<Block> phases)
            {
                phases.forEach(unwaxed -> {
                    Optional<Block> waxed = WaxHelper.getWaxedBlock(unwaxed);
                    waxed.ifPresent(block -> createShapeless(category, block)
                            .input(unwaxed)
                            .input(Items.HONEYCOMB)
                            .group(getItemPath(block))
                            .criterion(hasItem(unwaxed), conditionsFromItem(unwaxed))
                            .offerTo(exporter, convertBetween(block, Items.HONEYCOMB)));
                });
            }


            private void generateWaxing(RecipeExporter exporter, RecipeCategory category, Block... blocks)
            {
                Arrays.stream(blocks).forEach(unwaxed -> {
                    Optional<Block> waxed = WaxHelper.getWaxedBlock(unwaxed);
                    waxed.ifPresent(block -> createShapeless(category, block)
                            .input(unwaxed)
                            .input(Items.HONEYCOMB)
                            .group(getItemPath(block))
                            .criterion(hasItem(unwaxed), conditionsFromItem(unwaxed))
                            .offerTo(exporter, convertBetween(block, Items.HONEYCOMB)));
                });
            }

            private void generateMoonstoneRecipes()
            {
                List<Block> CHISELED_MOONSTONE = List.of(
                        Registration.CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK,
                        Registration.CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK,
                        Registration.CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK,
                        Registration.CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK,
                        Registration.CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK,
                        Registration.CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK,
                        Registration.CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK,
                        Registration.CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK
                );

                offerCrackingRecipe(Registration.CRACKED_MOONSTONE_BRICKS_ITEM, Registration.MOONSTONE_BRICKS_ITEM);
                offerSlabRecipe(RecipeCategory.BUILDING_BLOCKS, Registration.MOONSTONE_BRICK_SLAB_ITEM, Registration.MOONSTONE_BRICKS_ITEM);
                offerWallRecipe(RecipeCategory.BUILDING_BLOCKS, Registration.MOONSTONE_BRICK_WALL_ITEM, Registration.MOONSTONE_BRICKS_ITEM);


                createChiseledBlockRecipe(RecipeCategory.BUILDING_BLOCKS, Registration.CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK, Ingredient.ofItems(Registration.MOONSTONE_BRICK_SLAB_ITEM))
                        .group(getItemPath(Registration.CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK))
                        .criterion(hasItem(Registration.MOONSTONE_BRICK_SLAB_ITEM), conditionsFromItem(Registration.MOONSTONE_BRICK_SLAB_ITEM))
                        .offerTo(exporter);
                //offerChiseledBlockRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, Registration.CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK, Registration.MOONSTONE_BRICK_SLAB_ITEM);
                generateLunarCycleShapeless(exporter, RecipeCategory.BUILDING_BLOCKS, CHISELED_MOONSTONE);

                generateWaxing(exporter, RecipeCategory.BUILDING_BLOCKS,
                        Registration.MOONSTONE_BRICKS_BLOCK,
                        Registration.CRACKED_MOONSTONE_BRICKS_BLOCK,
                        Registration.MOONSTONE_BRICK_SLAB_BLOCK,
                        Registration.MOONSTONE_BRICK_WALL_BLOCK);
                generateLunarPhaseWaxing(exporter, RecipeCategory.BUILDING_BLOCKS, CHISELED_MOONSTONE);

                offerStonecuttingRecipe(RecipeCategory.BUILDING_BLOCKS, Registration.MOONSTONE_BRICKS_ITEM, Registration.MOONSTONE_BRICK_SLAB_ITEM, 2);
                offerStonecuttingRecipe(RecipeCategory.BUILDING_BLOCKS, Registration.MOONSTONE_BRICKS_ITEM, Registration.MOONSTONE_BRICK_STAIRS_ITEM);
                offerStonecuttingRecipe(RecipeCategory.BUILDING_BLOCKS, Registration.MOONSTONE_BRICKS_ITEM, Registration.MOONSTONE_BRICK_WALL_ITEM);
                offerStonecuttingRecipe(RecipeCategory.BUILDING_BLOCKS, Registration.MOONSTONE_BRICKS_ITEM, Registration.CRACKED_MOONSTONE_BRICKS_ITEM);
                generateLunarCycleStonecutting(RecipeCategory.BUILDING_BLOCKS, Registration.MOONSTONE_BRICKS_BLOCK, CHISELED_MOONSTONE);
            }

            @Override
            public void generate() {
//                RegistryEntryLookup<Item> itemLookup = wrapperLookup.getOrThrow(RegistryKeys.ITEM);

                generateMoonstoneRecipes();
            }
        };
    }

    @Override
    public String getName() {
        return "Groves Recipe Provider";
    }
}