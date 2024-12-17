package github.xevira.groves.data.provider;


import github.xevira.groves.Registration;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.data.server.tag.TagProvider;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.TagBuilder;

import java.util.concurrent.CompletableFuture;

public class ModDamageTypeTagProvider extends FabricTagProvider<DamageType> {

    public ModDamageTypeTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.DAMAGE_TYPE, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(DamageTypeTags.BYPASSES_ARMOR)
                .add(Registration.VOID_DAMAGE)
        ;

        getOrCreateTagBuilder(DamageTypeTags.BYPASSES_INVULNERABILITY)
                .add(Registration.VOID_DAMAGE)
        ;

        getOrCreateTagBuilder(DamageTypeTags.BYPASSES_INVULNERABILITY)
                .add(Registration.VOID_DAMAGE)
        ;

        getOrCreateTagBuilder(DamageTypeTags.ALWAYS_MOST_SIGNIFICANT_FALL)
                .add(Registration.VOID_DAMAGE)
        ;

        getOrCreateTagBuilder(DamageTypeTags.NO_KNOCKBACK)
                .add(Registration.SUFFOCATION_DAMAGE)
                .add(Registration.SUN_DAMAGE)
                .add(Registration.VOID_DAMAGE)
                .add(Registration.WATER_DAMAGE)
        ;

        getOrCreateTagBuilder(DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES)
                .add(Registration.WATER_DAMAGE)
        ;
    }
}
