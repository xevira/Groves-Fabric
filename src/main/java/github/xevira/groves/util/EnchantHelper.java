package github.xevira.groves.util;

import github.xevira.groves.Registration;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;

public class EnchantHelper {
    public static int getEnchantLevel(ItemStack stack, RegistryKey<Enchantment> key)
    {
        ItemEnchantmentsComponent enchants = stack.getEnchantments();
        if (enchants == null || enchants.isEmpty()) return 0;

        for(Object2IntMap.Entry<RegistryEntry<Enchantment>> enchant : enchants.getEnchantmentEntries())
        {
            RegistryEntry<Enchantment> registryEntry = enchant.getKey();

            if (registryEntry.getKey().isPresent() && registryEntry.getKey().get().equals(key))
                return enchant.getIntValue();
        }

        return 0;
    }
}
