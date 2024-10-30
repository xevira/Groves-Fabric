package github.xevira.groves.data.generator;

import github.xevira.groves.Registration;
import github.xevira.groves.enchantment.effects.SolarRepairEnchantmentEffect;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

public class EnchantmentGenerator extends FabricDynamicRegistryProvider {
    public EnchantmentGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
        RegistryWrapper<Item> itemLookup = registries.getOrThrow(RegistryKeys.ITEM);

        register(entries, Registration.SOLAR_REPAIR_ENCHANTMENT_KEY, Enchantment.builder(
            Enchantment.definition(
                    itemLookup.getOrThrow(ItemTags.DURABILITY_ENCHANTABLE),
                    1,
                    5,
                    Enchantment.leveledCost(35, 19),
                    Enchantment.leveledCost(1, 19),
                    7,
                    AttributeModifierSlot.ANY))
                .addEffect(EnchantmentEffectComponentTypes.TICK,
                        new SolarRepairEnchantmentEffect(EnchantmentLevelBasedValue.linear(1.0f, 1.0f)))
        );

    }

    private static void register(Entries entries, RegistryKey<Enchantment> key, Enchantment.Builder builder, ResourceCondition... resourceConditions) {
        entries.add(key, builder.build(key.getValue()), resourceConditions);
    }

    @Override
    public String getName() {
        return "Enchantment Generator";
    }
}
