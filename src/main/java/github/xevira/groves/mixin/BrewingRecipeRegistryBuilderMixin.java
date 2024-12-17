package github.xevira.groves.mixin;

import github.xevira.groves.Groves;
import github.xevira.groves.concoctions.brewing.BrewingRegistry;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingRecipeRegistry.Builder.class)
public class BrewingRecipeRegistryBuilderMixin implements FabricBrewingRecipeRegistryBuilder {
    @Shadow
    @Final
    private FeatureSet enabledFeatures;

    @Inject(method = "registerItemRecipe", at = @At("TAIL"))
    void injectRegisterItemRecipe(Item input, Item ingredient, Item output, CallbackInfo cb) {
        if (BrewingRegistry.INJECTIONS_ENABLED) {
            Groves.LOGGER.info("injectRegisterItemRecipe({}, {}, {}) called", input, ingredient, output);
            if (input.isEnabled(this.enabledFeatures) && output.isEnabled(this.enabledFeatures)) {
                // Do some stuff
                Groves.LOGGER.info("injectRegisterItemRecipe - do stuff");
            }
        }
    }

    @Inject(method = "registerPotionRecipe", at = @At("TAIL"))
    void injectRegisterPotionRecipe(RegistryEntry<Potion> input, Item ingredient, RegistryEntry<Potion> output, CallbackInfo cb) {
        if(BrewingRegistry.INJECTIONS_ENABLED) {
            if (input.value().isEnabled(this.enabledFeatures) && output.value().isEnabled(this.enabledFeatures)) {
                BrewingRegistry.registerPotionRecipe(input, ingredient, output);
            }
        }
    }
}
