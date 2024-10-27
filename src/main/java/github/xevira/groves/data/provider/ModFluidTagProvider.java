package github.xevira.groves.data.provider;

import github.xevira.groves.Registration;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModFluidTagProvider extends FabricTagProvider.FluidTagProvider {
    public ModFluidTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(Registration.BLESSED_MOON_WATERS_TAG)
                .add(Registration.BLESSED_MOON_WATER_FLUID)
                .add(Registration.FLOWING_BLESSED_MOON_WATER_FLUID);

        getOrCreateTagBuilder(Registration.MOONLIGHT_TAG)
                .add(Registration.MOONLIGHT_FLUID)
                .add(Registration.FLOWING_MOONLIGHT_FLUID);
    }
}
