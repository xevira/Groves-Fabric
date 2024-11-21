package github.xevira.groves.data.provider;

import github.xevira.groves.Registration;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture, @Nullable BlockTagProvider blockTagProvider) {
        super(output, completableFuture, blockTagProvider);
    }

    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(ItemTags.SWORDS)
                .add(Registration.SANCTUM_SWORD_ITEM)
                ;

        getOrCreateTagBuilder(ItemTags.PICKAXES)
                .add(Registration.SANCTUM_PICKAXE_ITEM)
                ;

        getOrCreateTagBuilder(ItemTags.AXES)
                .add(Registration.SANCTUM_AXE_ITEM)
                ;

        getOrCreateTagBuilder(ItemTags.SHOVELS)
                .add(Registration.SANCTUM_SHOVEL_ITEM)
                ;

        getOrCreateTagBuilder(ItemTags.HOES)
                .add(Registration.SANCTUM_HOE_ITEM)
                ;

        getOrCreateTagBuilder(Registration.SANCTUM_LOG_ITEMS)
                .add(Registration.SANCTUM_LOG_ITEM)
                .add(Registration.SANCTUM_WOOD_ITEM)
                .add(Registration.STRIPPED_SANCTUM_LOG_ITEM)
                .add(Registration.STRIPPED_SANCTUM_WOOD_ITEM)
                ;

        getOrCreateTagBuilder(Registration.SANCTUM_PLANKS_ITEMS)
                .add(Registration.SANCTUM_PLANKS_ITEM)
                ;
    }
}
