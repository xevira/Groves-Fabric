package github.xevira.groves.events;

import github.xevira.groves.Registration;
import github.xevira.groves.ServerConfig;
import github.xevira.groves.item.MoonPhialItem;
import github.xevira.groves.poi.GrovesPOI;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;

public class ModServerTickEvents {
    private static Random RNG = Random.create();

    public static void onEndServerTick(MinecraftServer server)
    {
        GrovesPOI.onEndServerTick(server);

        processSolarRepair(server);
    }

    public static void onStartWorldTick(World world)
    {
        if (world.getRegistryKey().getValue().equals(World.OVERWORLD.getValue()))
        {
            // Overworld
            MoonPhialItem.updateLunarPhase(world);
        }
    }

    private static boolean canPlayerSolarRepair(ServerPlayerEntity player)
    {
        ServerWorld world = player.getServerWorld();

        if (world == null) return false;

        // Must be day time
        if (!world.isDay()) return false;

        // Must be clear
        if (world.isRaining()) return false;

        // Only once a second
        if (world.getTime() % 20 != 0) return false;

        return world.isSkyVisible(player.getBlockPos());
    }

    private static int getSolarRepair(ItemStack stack)
    {
        ItemEnchantmentsComponent enchants = stack.getEnchantments();
        if (enchants == null || enchants.isEmpty()) return 0;

        for(Entry<RegistryEntry<Enchantment>> enchant : enchants.getEnchantmentEntries())
        {
            RegistryEntry<Enchantment> registryEntry = enchant.getKey();

            if (registryEntry.getKey().isPresent() && registryEntry.getKey().get().equals(Registration.SOLAR_REPAIR_ENCHANTMENT_KEY))
                return enchant.getIntValue();
        }

        return 0;
    }

    private static void processSolarRepair(ServerPlayerEntity player)
    {
        PlayerInventory inventory = player.getInventory();

        for(int i = 0; i < inventory.size(); i++)
        {
            ItemStack stack = inventory.getStack(i);

            if (stack.isEmpty() || !stack.isDamageable() || !stack.isDamaged()) continue;

            int solarRepair = getSolarRepair(stack);

            if (solarRepair > 0)
            {
                float chance = ServerConfig.getSolarRepairBaseChance() + (solarRepair - 1) * ServerConfig.getSolarRepairExtraChance();
                int damage = stack.getDamage();

                if (RNG.nextFloat() < chance)
                {
                    stack.setDamage(damage - 1);
                }
            }
         }
    }

    private static void processSolarRepair(MinecraftServer server)
    {
        server.getPlayerManager().getPlayerList().stream().filter(ModServerTickEvents::canPlayerSolarRepair).forEach(ModServerTickEvents::processSolarRepair);
    }
}
