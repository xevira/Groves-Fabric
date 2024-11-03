package github.xevira.groves.events;

import github.xevira.groves.Registration;
import github.xevira.groves.ServerConfig;
import github.xevira.groves.item.MoonPhialItem;
import github.xevira.groves.network.SanctuaryEnterPayload;
import github.xevira.groves.poi.GrovesPOI;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ModServerTickEvents {
    private static Map<UUID, PlayerTracking> PLAYER_TRACKING = new HashMap<>();

    private static Random RNG = Random.create();

    public static void onServerStarted(MinecraftServer server)
    {
        PLAYER_TRACKING.clear();
    }

    private static void updatePlayerChunk(ServerPlayerEntity player)
    {
        BlockPos pos = player.getBlockPos();
        ChunkPos newChunk = new ChunkPos(pos);

        boolean entry;
        PlayerTracking tracking;

        if (PLAYER_TRACKING.containsKey(player.getUuid()))
        {
            entry = true;
            tracking = PLAYER_TRACKING.get(player.getUuid());

            // Same world&chunk as before
            if (tracking.world == player.getServerWorld() && newChunk.equals(tracking.chunk)) return;

            tracking.world = player.getServerWorld();
            tracking.chunk = newChunk;
        }
        else
        {
            entry = false;
            tracking = new PlayerTracking(player.getServerWorld(), newChunk);

            PLAYER_TRACKING.put(player.getUuid(), tracking);
        }

        GrovesPOI.GroveSanctuary newSanctuary = GrovesPOI.getSanctuary(player.getServerWorld(), newChunk).orElse(null);

        GrovesPOI.GroveSanctuary oldSanctuary = tracking.sanctuary;

        // Same sanctuary as before
        if (newSanctuary == oldSanctuary)
            return;

        tracking.sanctuary = newSanctuary;

        if (newSanctuary != null)
            ServerPlayNetworking.send(player, new SanctuaryEnterPayload(newSanctuary.getOwner(), newSanctuary.getOwnerName(), newSanctuary.getGroveName(), newSanctuary.isAbandoned(), entry));
    }

    public static void onEndServerTick(MinecraftServer server)
    {
        GrovesPOI.onEndServerTick(server);

        processSolarRepair(server);

        processSanctuaryEntry(server);
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

    private static void processSanctuaryEntry(MinecraftServer server)
    {
        server.getPlayerManager().getPlayerList().forEach(ModServerTickEvents::updatePlayerChunk);
    }

    private static class PlayerTracking {
        public ServerWorld world;
        public ChunkPos chunk;
        public GrovesPOI.GroveSanctuary sanctuary;

        public PlayerTracking(ServerWorld world, ChunkPos pos)
        {
            this.world = world;
            this.chunk = pos;
            this.sanctuary = null;
        }

    }
}
