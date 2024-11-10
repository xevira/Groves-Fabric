package github.xevira.groves.data.generator;

import github.xevira.groves.Registration;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.effect.AllOfEnchantmentEffects;
import net.minecraft.enchantment.effect.EnchantmentEffectTarget;
import net.minecraft.enchantment.effect.entity.PlaySoundEnchantmentEffect;
import net.minecraft.enchantment.effect.entity.SummonEntityEnchantmentEffect;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.floatprovider.ConstantFloatProvider;

import java.util.concurrent.CompletableFuture;

public class EnchantmentGenerator extends FabricDynamicRegistryProvider {
    public EnchantmentGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
        RegistryWrapper<Item> itemLookup = registries.getOrThrow(RegistryKeys.ITEM);

        register(entries, Registration.LIGHT_FOOTED_ENCHANTMENT_KEY, Enchantment.builder(
            Enchantment.definition(
                    itemLookup.getOrThrow(ItemTags.FOOT_ARMOR),
                    1,
                    1,
                    Enchantment.leveledCost(35, 19),
                    Enchantment.leveledCost(1, 19),
                    7,
                    AttributeModifierSlot.FEET)));

        register(entries, Registration.SOLAR_REPAIR_ENCHANTMENT_KEY, Enchantment.builder(
            Enchantment.definition(
                    itemLookup.getOrThrow(ItemTags.DURABILITY_ENCHANTABLE),
                    1,
                    5,
                    Enchantment.leveledCost(35, 19),
                    Enchantment.leveledCost(1, 19),
                    7,
                    AttributeModifierSlot.ANY))
                //.addEffect(EnchantmentEffectComponentTypes.TICK, new SolarRepairEnchantmentEffect(EnchantmentLevelBasedValue.linear(1.0f, 1.0f)))
        );

        register(entries, Registration.THUNDERING_ENCHANTMENT_KEY, Enchantment.builder(
                        Enchantment.definition(
                                itemLookup.getOrThrow(ItemTags.MACE_ENCHANTABLE),
                                15,
                                1,
                                Enchantment.leveledCost(1, 10),
                                Enchantment.leveledCost(1, 15),
                                7,
                                AttributeModifierSlot.HAND))
                .addEffect(
                        EnchantmentEffectComponentTypes.POST_ATTACK,
                        EnchantmentEffectTarget.ATTACKER,
                        EnchantmentEffectTarget.VICTIM,
                        AllOfEnchantmentEffects.allOf(
                                new SummonEntityEnchantmentEffect(RegistryEntryList.of(EntityType.LIGHTNING_BOLT.getRegistryEntry()), false),
                                new PlaySoundEnchantmentEffect(Registration.MACE_THUNDERING_SOUND, ConstantFloatProvider.create(5.0F), ConstantFloatProvider.create(1.0F))
                        )
                ));
    }

    private static void register(Entries entries, RegistryKey<Enchantment> key, Enchantment.Builder builder, ResourceCondition... resourceConditions) {
        entries.add(key, builder.build(key.getValue()), resourceConditions);
    }

    @Override
    public String getName() {
        return "Enchantment Generator";
    }
}
