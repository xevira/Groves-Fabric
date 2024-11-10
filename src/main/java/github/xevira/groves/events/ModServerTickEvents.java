package github.xevira.groves.events;

import github.xevira.groves.Groves;
import github.xevira.groves.Registration;
import github.xevira.groves.ServerConfig;
import github.xevira.groves.item.MoonPhialItem;
import github.xevira.groves.network.SanctuaryDarknessPayload;
import github.xevira.groves.network.SanctuaryEnterPayload;
import github.xevira.groves.network.SanctuarySunlightPayload;
import github.xevira.groves.poi.GrovesPOI;
import github.xevira.groves.util.EnchantHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ModServerTickEvents {
    private static final Map<UUID, PlayerTracking> PLAYER_TRACKING = new HashMap<>();

    private static final Random RNG = Random.create();

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
        GrovesPOI.GroveSanctuary newSanctuary = GrovesPOI.getSanctuary(player.getServerWorld(), newChunk).orElse(null);

        if (PLAYER_TRACKING.containsKey(player.getUuid()))
        {
            entry = true;
            tracking = PLAYER_TRACKING.get(player.getUuid());

            // Same world&chunk as before
            if (tracking.world == player.getServerWorld() && newChunk.equals(tracking.chunk)) {
                if (tracking.sanctuary != newSanctuary)
                    entry = false;
            }
            else {
                tracking.world = player.getServerWorld();
                tracking.chunk = newChunk;
            }
        }
        else
        {
            entry = false;
            tracking = new PlayerTracking(player.getServerWorld(), newChunk);

            PLAYER_TRACKING.put(player.getUuid(), tracking);
        }

        int sunlight;
        int darkness;

        if (newSanctuary != null && newSanctuary.isOwner(player))
        {
            sunlight = newSanctuary.getSunlightPercent();
            darkness = newSanctuary.getDarknessPercent();
        }
        else
        {
            sunlight = -1;
            darkness = -1;
        }

        if (tracking.sanctuary != newSanctuary) {
            tracking.sanctuary = newSanctuary;

            if (newSanctuary != null) {
                ServerPlayNetworking.send(player, new SanctuaryEnterPayload(newSanctuary.getOwner(), newSanctuary.getOwnerName(), newSanctuary.getGroveName(), newSanctuary.isAbandoned(), entry));
            }
        }

        if (tracking.sunlightPercent != sunlight) {
            tracking.sunlightPercent = sunlight;
            ServerPlayNetworking.send(player, new SanctuarySunlightPayload(sunlight));
        }

        if (tracking.darknessPercent != darkness) {
            tracking.darknessPercent = darkness;
            ServerPlayNetworking.send(player, new SanctuaryDarknessPayload(darkness));
        }
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

    private static void processSolarRepair(ServerPlayerEntity player)
    {
        PlayerInventory inventory = player.getInventory();

        for(int i = 0; i < inventory.size(); i++)
        {
            ItemStack stack = inventory.getStack(i);

            if (stack.isEmpty() || !stack.isDamageable() || !stack.isDamaged()) continue;

            int solarRepair = EnchantHelper.getEnchantLevel(stack, Registration.SOLAR_REPAIR_ENCHANTMENT_KEY);
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
        public int sunlightPercent;
        public int darknessPercent;

        public PlayerTracking(ServerWorld world, ChunkPos pos)
        {
            this.world = world;
            this.chunk = pos;
            this.sanctuary = null;
            this.sunlightPercent = -1;
            this.darknessPercent = -1;
        }
    }
}
