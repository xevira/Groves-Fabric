package github.xevira.groves.data.provider;

import com.google.common.collect.ImmutableList;
import github.xevira.groves.Registration;
import github.xevira.groves.item.UnlockScrollItem;
import github.xevira.groves.sanctuary.GroveAbilities;
import github.xevira.groves.sanctuary.GroveAbility;
import github.xevira.groves.util.WaxHelper;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.block.Block;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.jetbrains.annotations.NotNull;

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
            private void generateUnlockScrollRecipes()
            {
                createShaped(RecipeCategory.MISC, Registration.UNLOCK_SCROLL_ITEM)
                        .input('p', Items.PAPER)
                        .input('a', Registration.AQUAMARINE_DUST_ITEM)
                        .pattern(" a ")
                        .pattern("apa")
                        .pattern(" a ")
                        .criterion(hasItem(Items.PAPER), conditionsFromItem(Items.PAPER))
                        .criterion(hasItem(Registration.AQUAMARINE_DUST_ITEM), conditionsFromItem(Registration.AQUAMARINE_DUST_ITEM))
                        .offerTo(exporter);

                for(GroveAbility ability : GroveAbilities.ABILITIES.values())
                {
                    if (ability.isForbidden()) continue;    // Forbidden scrolls are *never* craftable

                    List<UnlockScrollItem> scrolls = GroveAbilities.UNLOCK_SCROLLS.get(ability.getName());
                    if (scrolls != null) {
                        for (int i = 0; i < scrolls.size(); i++) {
                            Item ingredient = ability.getRecipeIngredient(i + 1);
                            if (ingredient != null) {
                                createShaped(RecipeCategory.MISC, scrolls.get(i))
                                        .input('#', ingredient)
                                        .input('S', Registration.UNLOCK_SCROLL_ITEM)
                                        .input('I', Items.INK_SAC)
                                        .pattern(" # ")
                                        .pattern("ISI")
                                        .pattern(" I ")
                                        .criterion(hasItem(ingredient), conditionsFromItem(ingredient))
                                        .criterion(hasItem(Registration.UNLOCK_SCROLL_ITEM), conditionsFromItem(Registration.UNLOCK_SCROLL_ITEM))
                                        .criterion(hasItem(Items.INK_SAC), conditionsFromItem(Items.INK_SAC))
                                        .offerTo(exporter);
                            }
                        }
                    }

                }
            }

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

            private void sword(Item item, Item sword)
            {
                createShaped(RecipeCategory.COMBAT, sword)
                        .input('i', item)
                        .input('s', ConventionalItemTags.WOODEN_RODS)
                        .pattern("i")
                        .pattern("i")
                        .pattern("s")
                        .criterion(hasItem(item), conditionsFromItem(item))
                        .criterion(hasTag(ConventionalItemTags.WOODEN_RODS), conditionsFromTag(ConventionalItemTags.WOODEN_RODS))
                        .offerTo(exporter);
            }

            private void pickaxe(Item item, Item pickaxe)
            {
                createShaped(RecipeCategory.TOOLS, pickaxe)
                        .input('i', item)
                        .input('s', ConventionalItemTags.WOODEN_RODS)
                        .pattern("iii")
                        .pattern(" s ")
                        .pattern(" s ")
                        .criterion(hasItem(item), conditionsFromItem(item))
                        .criterion(hasTag(ConventionalItemTags.WOODEN_RODS), conditionsFromTag(ConventionalItemTags.WOODEN_RODS))
                        .offerTo(exporter);
            }

            private void axe(Item item, Item axe)
            {
                createShaped(RecipeCategory.TOOLS, axe)
                        .input('i', item)
                        .input('s', ConventionalItemTags.WOODEN_RODS)
                        .pattern("ii ")
                        .pattern("is ")
                        .pattern(" s ")
                        .criterion(hasItem(item), conditionsFromItem(item))
                        .criterion(hasTag(ConventionalItemTags.WOODEN_RODS), conditionsFromTag(ConventionalItemTags.WOODEN_RODS))
                        .offerTo(exporter);
            }

            private void shovel(Item item, Item shovel)
            {
                createShaped(RecipeCategory.TOOLS, shovel)
                        .input('i', item)
                        .input('s', ConventionalItemTags.WOODEN_RODS)
                        .pattern("i")
                        .pattern("s")
                        .pattern("s")
                        .criterion(hasItem(item), conditionsFromItem(item))
                        .criterion(hasTag(ConventionalItemTags.WOODEN_RODS), conditionsFromTag(ConventionalItemTags.WOODEN_RODS))
                        .offerTo(exporter);
            }

            private void hoe(Item item, Item hoe)
            {
                createShaped(RecipeCategory.TOOLS, hoe)
                        .input('i', item)
                        .input('s', ConventionalItemTags.WOODEN_RODS)
                        .pattern("ii")
                        .pattern(" s")
                        .pattern(" s")
                        .criterion(hasItem(item), conditionsFromItem(item))
                        .criterion(hasTag(ConventionalItemTags.WOODEN_RODS), conditionsFromTag(ConventionalItemTags.WOODEN_RODS))
                        .offerTo(exporter);
            }

            private void planks(TagKey<Item> logs, Block planks)
            {
                createShapeless(RecipeCategory.BUILDING_BLOCKS, planks, 4)
                        .input(logs)
                        .criterion(hasTag(logs), conditionsFromTag(logs))
                        .offerTo(exporter);
            }

            private void slab(Block block, Block slab)
            {
                createShaped(RecipeCategory.BUILDING_BLOCKS, slab, 6)
                        .input('b', block)
                        .pattern("bbb")
                        .criterion(hasItem(block), conditionsFromItem(block))
                        .offerTo(exporter);
            }

            private void stairs(Block block, Block stairs)
            {
                createShaped(RecipeCategory.BUILDING_BLOCKS, stairs, 4)
                        .input('b', block)
                        .pattern("b  ")
                        .pattern("bb ")
                        .pattern("bbb")
                        .criterion(hasItem(block), conditionsFromItem(block))
                        .offerTo(exporter);
            }

            private void door(Block block, Block door)
            {
                createShaped(RecipeCategory.BUILDING_BLOCKS, door, 3)
                        .input('b', block)
                        .pattern("bb")
                        .pattern("bb")
                        .pattern("bb")
                        .criterion(hasItem(block), conditionsFromItem(block))
                        .offerTo(exporter);
            }

            private void trapdoor(Block block, Block trapdoor)
            {
                createShaped(RecipeCategory.BUILDING_BLOCKS, trapdoor, 2)
                        .input('b', block)
                        .pattern("bb")
                        .pattern("bb")
                        .criterion(hasItem(block), conditionsFromItem(block))
                        .offerTo(exporter);
            }

            private void pressureplate(Block block, Block plate)
            {
                createShaped(RecipeCategory.REDSTONE, plate, 2)
                        .input('b', block)
                        .pattern("bb")
                        .criterion(hasItem(block), conditionsFromItem(block))
                        .offerTo(exporter);
            }

            private void button(Block block, Block button)
            {
                createShapeless(RecipeCategory.REDSTONE, button)
                        .input(block)
                        .criterion(hasItem(block), conditionsFromItem(block))
                        .offerTo(exporter);
            }

            private void fence(Block block, Block fence)
            {
                createShaped(RecipeCategory.BUILDING_BLOCKS, fence, 3)
                        .input('b', block)
                        .input('s', ConventionalItemTags.WOODEN_RODS)
                        .pattern("bsb")
                        .pattern("bsb")
                        .criterion(hasItem(block), conditionsFromItem(block))
                        .criterion(hasTag(ConventionalItemTags.WOODEN_RODS), conditionsFromTag(ConventionalItemTags.WOODEN_RODS))
                        .offerTo(exporter);
            }

            private void gate(Block block, Block gate)
            {
                createShaped(RecipeCategory.BUILDING_BLOCKS, gate)
                        .input('b', block)
                        .input('s', ConventionalItemTags.WOODEN_RODS)
                        .pattern("sbs")
                        .pattern("sbs")
                        .criterion(hasItem(block), conditionsFromItem(block))
                        .criterion(hasTag(ConventionalItemTags.WOODEN_RODS), conditionsFromTag(ConventionalItemTags.WOODEN_RODS))
                        .offerTo(exporter);
            }

            private void sign(Block block, Block sign)
            {
                createShaped(RecipeCategory.DECORATIONS, sign, 3)
                        .input('b', block)
                        .input('s', ConventionalItemTags.WOODEN_RODS)
                        .pattern("bbb")
                        .pattern("bbb")
                        .pattern(" s ")
                        .criterion(hasItem(block), conditionsFromItem(block))
                        .criterion(hasTag(ConventionalItemTags.WOODEN_RODS), conditionsFromTag(ConventionalItemTags.WOODEN_RODS))
                        .offerTo(exporter);
            }

            private void hanging(Block block, Block sign)
            {
                createShaped(RecipeCategory.DECORATIONS, sign, 6)
                        .input('b', block)
                        .input('c', ConventionalItemTags.CHAINS)
                        .pattern("c c")
                        .pattern("bbb")
                        .pattern("bbb")
                        .criterion(hasItem(block), conditionsFromItem(block))
                        .criterion(hasTag(ConventionalItemTags.CHAINS), conditionsFromTag(ConventionalItemTags.CHAINS))
                        .offerTo(exporter);
            }

            private void boat(Block block, Item boat)
            {
                createShaped(RecipeCategory.TRANSPORTATION, boat)
                        .input('b', block)
                        .pattern("b b")
                        .pattern("bbb")
                        .criterion(hasItem(block), conditionsFromItem(block))
                        .offerTo(exporter);
            }

            private void chestboat(Item boat, Item chestboat)
            {
                createShapeless(RecipeCategory.TRANSPORTATION, chestboat)
                        .input(boat)
                        .input(ConventionalItemTags.WOODEN_CHESTS)
                        .criterion(hasItem(boat), conditionsFromItem(boat))
                        .criterion(hasTag(ConventionalItemTags.WOODEN_CHESTS), conditionsFromTag(ConventionalItemTags.WOODEN_CHESTS))
                        .offerTo(exporter);
            }

            private void generateSanctumRecipes()
            {
                createShaped(RecipeCategory.MISC, Items.STICK, 4)
                        .input('p', Registration.SANCTUM_PLANKS_ITEM)
                        .pattern("p")
                        .pattern("p")
                        .criterion(hasItem(Registration.SANCTUM_PLANKS_ITEM), conditionsFromItem(Registration.SANCTUM_PLANKS_ITEM))
                        .offerTo(exporter);

                planks(Registration.SANCTUM_LOG_ITEMS, Registration.SANCTUM_PLANKS_BLOCK);
                slab(Registration.SANCTUM_PLANKS_BLOCK, Registration.SANCTUM_SLAB_BLOCK);
                stairs(Registration.SANCTUM_PLANKS_BLOCK, Registration.SANCTUM_STAIRS_BLOCK);
                door(Registration.SANCTUM_PLANKS_BLOCK, Registration.SANCTUM_DOOR_BLOCK);
                trapdoor(Registration.SANCTUM_PLANKS_BLOCK, Registration.SANCTUM_TRAPDOOR_BLOCK);
                fence(Registration.SANCTUM_PLANKS_BLOCK, Registration.SANCTUM_FENCE_BLOCK);
                gate(Registration.SANCTUM_PLANKS_BLOCK, Registration.SANCTUM_FENCE_GATE_BLOCK);
                button(Registration.SANCTUM_PLANKS_BLOCK, Registration.SANCTUM_BUTTON_BLOCK);
                pressureplate(Registration.SANCTUM_PLANKS_BLOCK, Registration.SANCTUM_PRESSURE_PLATE_BLOCK);
                sign(Registration.SANCTUM_PLANKS_BLOCK, Registration.SANCTUM_SIGN_BLOCK);
                hanging(Registration.SANCTUM_PLANKS_BLOCK, Registration.SANCTUM_HANGING_SIGN_BLOCK);
                boat(Registration.SANCTUM_PLANKS_BLOCK, Registration.SANCTUM_BOAT_ITEM);
                chestboat(Registration.SANCTUM_BOAT_ITEM, Registration.SANCTUM_CHEST_BOAT_ITEM);

                sword(Registration.SANCTUM_PLANKS_ITEM, Registration.SANCTUM_SWORD_ITEM);
                pickaxe(Registration.SANCTUM_PLANKS_ITEM, Registration.SANCTUM_PICKAXE_ITEM);
                axe(Registration.SANCTUM_PLANKS_ITEM, Registration.SANCTUM_AXE_ITEM);
                shovel(Registration.SANCTUM_PLANKS_ITEM, Registration.SANCTUM_SHOVEL_ITEM);
                hoe(Registration.SANCTUM_PLANKS_ITEM, Registration.SANCTUM_HOE_ITEM);

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

                generateFamily(sanctumFamily, FeatureSet.empty());
            }

            private void smelt(List<ItemConvertible> inputs, RecipeCategory category, ItemConvertible output, float experience, int cookingTime, String group)
            {
                offerSmelting(inputs, category, output, experience, cookingTime, group);
                offerBlasting(inputs, category, output, 0.5f * experience, cookingTime / 2, group);
            }

            @Override
            public void generate() {

                generateMoonstoneRecipes();
                generateUnlockScrollRecipes();
                generateSanctumRecipes();

                createShaped(RecipeCategory.MISC, Registration.IMPRINTING_SIGIL_ITEM, 1)
                        .input('a', Registration.AQUAMARINE_ITEM)
                        .input('i', Items.IRON_INGOT)
                        .input('s', ItemTags.SAPLINGS)
                        .pattern("sis")
                        .pattern("iai")
                        .pattern("sis")
                        .criterion(hasItem(Registration.AQUAMARINE_ITEM), conditionsFromItem(Registration.AQUAMARINE_ITEM))
                        .criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
                        .criterion("has_sapling", conditionsFromTag(ItemTags.SAPLINGS))
                        .offerTo(exporter);


                createShapeless(RecipeCategory.MISC, Registration.AQUAMARINE_DUST_ITEM, 2)
                        .input(Registration.AQUAMARINE_ITEM)
                        .criterion(hasItem(Registration.AQUAMARINE_ITEM), conditionsFromItem(Registration.AQUAMARINE_ITEM))
                        .offerTo(exporter);

                ImmutableList<ItemConvertible> AQUAMARINE_ORES = ImmutableList.of(Registration.AQUAMARINE_ORE_ITEM, Registration.DEEPSLATE_AQUAMARINE_ORE_ITEM);
                smelt(AQUAMARINE_ORES, RecipeCategory.MISC, Registration.AQUAMARINE_ITEM, 1.0F, 200, "aquamarine");
                offerReversibleCompactingRecipesWithReverseRecipeGroup(RecipeCategory.MISC, Registration.AQUAMARINE_ITEM, RecipeCategory.BUILDING_BLOCKS, Registration.AQUAMARINE_BLOCK_ITEM, "aquamarine_from_aquamarine_block", "aquamarine");

                ImmutableList<ItemConvertible> IRONWOOD_SHARDS = ImmutableList.of(Registration.IRONWOOD_SHARD_ITEM);
                smelt(IRONWOOD_SHARDS, RecipeCategory.MISC, Items.IRON_NUGGET, 1.0F, 200, "ironwood_shard");

                createShaped(RecipeCategory.MISC, Registration.WIND_CHIME_BLOCK)
                        .input('s', Registration.SANCTUM_SLAB_ITEM)
                        .input('c', ConventionalItemTags.CHAINS)
                        .input('i', ConventionalItemTags.IRON_INGOTS)
                        .pattern("sss")
                        .pattern("c c")
                        .pattern("i i")
                        .criterion(hasItem(Registration.SANCTUM_SLAB_ITEM), conditionsFromItem(Registration.SANCTUM_SLAB_ITEM))
                        .criterion(hasTag(ConventionalItemTags.CHAINS), conditionsFromTag(ConventionalItemTags.CHAINS))
                        .criterion(hasTag(ConventionalItemTags.IRON_INGOTS), conditionsFromTag(ConventionalItemTags.IRON_INGOTS))
                        .offerTo(exporter);

            }
        };
    }

    @Override
    public String getName() {
        return "Recipe Provider";
    }

    private static @NotNull String hasTag(@NotNull TagKey<Item> tag) {
        return "has_" + tag.id().toString();
    }
}
