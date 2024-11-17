package github.xevira.groves.poi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import github.xevira.groves.Groves;
import github.xevira.groves.sanctuary.GroveAbilities;
import github.xevira.groves.sanctuary.GroveAbility;
import github.xevira.groves.sanctuary.GroveSanctuary;
import github.xevira.groves.util.JSONHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.IntStream;

/** Manages the POIs for player-made Grove Sanctuaries **/
public class GrovesPOI {
    private static final String JSON_SANCTUARIES = "sanctuaries";
    private static final String JSON_AVAILABLE = "available";

    private static final List<GroveSanctuary> SANCTUARIES = new ArrayList<>();

    private static final Map<RegistryKey<World>, Map<ChunkPos, GroveSanctuary>> CHUNK_MAP = new HashMap<>();

    private static final Map<RegistryKey<World>, Set<ChunkPos>> AVAILABILITY = new HashMap<>();

    public static void cleanup()
    {
        SANCTUARIES.clear();
        CHUNK_MAP.clear();
        AVAILABILITY.clear();
    }

    public static void onPlayerConnect(ServerPlayerEntity player)
    {
        SANCTUARIES.forEach(sanctuary -> sanctuary.syncColorData(player));
    }

    public static void registerGroveChunk(World world, ChunkPos pos, GroveSanctuary sanctuary)
    {
        Groves.LOGGER.info("registerGroveChunk({}, {}, {})", world.getRegistryKey().getValue().toString(), pos, sanctuary.getUUID());
        Map<ChunkPos, GroveSanctuary> chunks;
        if (CHUNK_MAP.containsKey(world.getRegistryKey()))
        {
            chunks = CHUNK_MAP.get(world.getRegistryKey());
}
        else
        {
            chunks = new HashMap<>();
            CHUNK_MAP.put(world.getRegistryKey(), chunks);
        }
        chunks.put(pos, sanctuary);
    }

    public static void unregisterGroveChunk(World world, ChunkPos pos)
    {
        if (CHUNK_MAP.containsKey(world.getRegistryKey()))
        {
            Map<ChunkPos, GroveSanctuary> chunks = CHUNK_MAP.get(world.getRegistryKey());

            chunks.remove(pos);
        }
    }

    public static Optional<GroveSanctuary> getSanctuary(PlayerEntity player)
    {
        return SANCTUARIES.stream().filter(groveSanctuary -> groveSanctuary.isOwner(player)).findFirst();
    }

    public static boolean hasSanctuary(PlayerEntity player)
    {
        return SANCTUARIES.stream().anyMatch(groveSanctuary -> groveSanctuary.isOwner(player));
    }

    public static Optional<GroveSanctuary> getSanctuary(World world, ChunkPos pos)
    {
//        return SANCTUARIES.stream().filter(groveSanctuary -> groveSanctuary.contains(world, pos)).findFirst();

        if (CHUNK_MAP.containsKey(world.getRegistryKey()))
        {
            Map<ChunkPos, GroveSanctuary> chunks = CHUNK_MAP.get(world.getRegistryKey());

            if (chunks.containsKey(pos))
                return Optional.of(chunks.get(pos));
        }

        return Optional.empty();
    }

    public static Optional<GroveSanctuary> getSanctuary(World world, BlockPos pos)
    {
//        return SANCTUARIES.stream().filter(groveSanctuary -> groveSanctuary.contains(world, new ChunkPos(pos))).findFirst();
        return getSanctuary(world, new ChunkPos(pos));
    }

    public static Optional<GroveSanctuary> getSanctuaryAbandoned(World world, BlockPos pos)
    {
//        return SANCTUARIES.stream().filter(groveSanctuary -> groveSanctuary.contains(world, new ChunkPos(pos)) && groveSanctuary.isAbandoned()).findFirst();
        Optional<GroveSanctuary> sanctuary = getSanctuary(world, pos);

        if (sanctuary.isPresent() && sanctuary.get().isAbandoned())
            return sanctuary;

        return Optional.empty();
    }

    public static boolean sanctuaryExists(World world, ChunkPos pos)
    {
//        return SANCTUARIES.stream().anyMatch(groveSanctuary -> groveSanctuary.contains(world, pos));

        return getSanctuary(world, pos).isPresent();
    }

    public static void claimAvailable(World world, ChunkPos pos)
    {
        if (!AVAILABILITY.containsKey(world.getRegistryKey()))
            AVAILABILITY.put(world.getRegistryKey(), new HashSet<>());

        Set<ChunkPos> chunks = AVAILABILITY.get(world.getRegistryKey());

        chunks.add(pos);
    }

    public static void releaseAvailable(World world, ChunkPos pos)
    {
        if (AVAILABILITY.containsKey(world.getRegistryKey()))
        {
            Set<ChunkPos> chunks = AVAILABILITY.get(world.getRegistryKey());

            chunks.remove(pos);
        }
    }

    public static boolean chunkAvailable(World world, ChunkPos pos)
    {
        Set<ChunkPos> chunks = AVAILABILITY.get(world.getRegistryKey());

        return chunks != null && chunks.contains(pos);
    }

    public static Either<GroveSanctuary, ImprintSanctuaryResult> imprintSanctuary(PlayerEntity player, ServerWorld world, BlockPos pos, boolean enchanted)
    {
        ChunkPos chunkPos = new ChunkPos(pos);

        if (hasSanctuary(player))
            return Either.right(ImprintSanctuaryResult.ALREADY_HAS_GROVE);

        GroveSanctuary sanctuary = getSanctuary(world, chunkPos).orElse(null);
        if (sanctuary != null) {
            if (sanctuary.isOwner(player))
                return Either.right(ImprintSanctuaryResult.ALREADY_HAS_GROVE);

            // Allow for reclaiming an abandoned sanctuary
            if (sanctuary.isAbandoned()) {
                if (sanctuary.reclaimSanctuary(player, enchanted))
                    return Either.left(sanctuary);
                else
                    return Either.right(ImprintSanctuaryResult.ABANDONED);
            }

            return Either.right(ImprintSanctuaryResult.ALREADY_EXISTS);
        }

        // Create the Grove Sanctuary
        sanctuary = new GroveSanctuary(world.getServer(), player, world, chunkPos, enchanted);
        SANCTUARIES.add(sanctuary);

        // Register the origin chunk
        registerGroveChunk(world, chunkPos, sanctuary);

        // Set up the availability for the origin chunk
        sanctuary.calculateAvailable(chunkPos);

        // Default spawn point of the grove.
        sanctuary.setSpawnPoint(pos);

        GroveAbilities.autoInstallAbilities(sanctuary);

        sanctuary.updateColorData();

        return Either.left(sanctuary);
    }

    private static boolean isPosValidForGrove(ServerWorld world, BlockPos pos, boolean enchanted)
    {
        RegistryEntry<Biome> biome = world.getBiome(pos);

        // TODO: Add use for enchanted to allow imprinting an Ancient Grove biome.

        return biome.isIn(BiomeTags.IS_FOREST);
    }

    /** Checks if at least 50% of the chunk is forested. **/
    public static boolean isChunkValidForGrove(ServerWorld world, ChunkPos chunkPos, boolean enchanted)
    {
        int x = chunkPos.x * 16;
        int z = chunkPos.z * 16;

        int forested = 0;
        for(int ns = 0; ns < 16; ns++)
        {
            for(int ew = 0; ew < 16; ew++)
            {
                BlockPos pos = new BlockPos(x + ew, 64, z + ns);

                if (isPosValidForGrove(world, pos, enchanted))
                    ++forested;
            }
        }

        return forested >= 128;
    }

    public static boolean isValidGroveLocation(ServerWorld world, BlockPos pos, boolean enchanted)
    {
        return isChunkValidForGrove(world, new ChunkPos(pos), enchanted);
    }

    public static void onEndServerTick(MinecraftServer server)
    {
        SANCTUARIES.forEach(sanctuary -> sanctuary.onEndServerTick(server));
    }

    public static void serializeServer(JsonObject json, MinecraftServer server)
    {
        JsonArray sanctuaries = new JsonArray();

        SANCTUARIES.stream().map(sanctuary -> sanctuary.serialize(server)).forEach(sanctuaries::add);

        json.add(JSON_SANCTUARIES, sanctuaries);

        JsonObject availableWorlds = new JsonObject();

        for(RegistryKey<World> worldKey : AVAILABILITY.keySet())
        {
            Set<ChunkPos> chunks = AVAILABILITY.get(worldKey);
            JsonArray array = new JsonArray();

            for(ChunkPos chunk : chunks)
            {
                array.add(JSONHelper.ChunkPosToJson(chunk));
            }

            availableWorlds.add(worldKey.getValue().toString(), array);
        }

        json.add(JSON_AVAILABLE, availableWorlds);
    }

    private static boolean deserializeSanctuaries(JsonObject json, MinecraftServer server)
    {
        SANCTUARIES.clear();
        CHUNK_MAP.clear();

        if (json.has(JSON_SANCTUARIES))
        {
            JsonElement element = json.get(JSON_SANCTUARIES);

            if (element.isJsonArray())
            {
                JsonArray array = element.getAsJsonArray();

                List<GroveSanctuary> sanctuaries = new ArrayList<>();

                for(JsonElement item : array)
                {
                    if (item.isJsonObject())
                    {
                        Optional<GroveSanctuary> sanctuary = github.xevira.groves.sanctuary.GroveSanctuary.deserialize(item.getAsJsonObject(), server);

                        if (sanctuary.isPresent())
                        {
                            sanctuaries.add(sanctuary.get());
                        }
                        else
                        {
                            Groves.LOGGER.error("Failed to load Grove Sanctuary.  Skipping.");
                        }
                    }
                    else
                    {
                        Groves.LOGGER.error("Failed to load Grove Sanctuary.  Skipping.");
                    }
                }

                SANCTUARIES.addAll(sanctuaries);
                Groves.LOGGER.info("Loaded {} Grove Sanctuaries", SANCTUARIES.size());
                return true;
            }

            return false;
        }

        Groves.LOGGER.info("Loaded {} Grove Sanctuaries", SANCTUARIES.size());
        return true;
    }

    private static boolean deserializeAvailable(JsonObject json, MinecraftServer server)
    {
        AVAILABILITY.clear();
        if (json.has(JSON_AVAILABLE)) {
            JsonElement element = json.get(JSON_AVAILABLE);

            if (element.isJsonObject())
            {
                JsonObject obj = element.getAsJsonObject();

                Map<String, JsonElement> map = obj.asMap();

                for(String key : map.keySet())
                {
                    RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(key));
                    Set<ChunkPos> chunks = new HashSet<>();

                    JsonElement element2 = map.get(key);

                    if (element2.isJsonArray())
                    {
                        JsonArray array = element2.getAsJsonArray();

                        for(JsonElement item : array)
                        {
                            if (item.isJsonObject())
                            {
                                Optional<ChunkPos> pos = JSONHelper.JsonToChunkPos(item.getAsJsonObject());

                                pos.ifPresent(chunks::add);
                            }
                        }
                    }

                    AVAILABILITY.put(worldKey, chunks);
                }
                return true;
            }
            return false;
        }

        return true;
    }

    public static boolean deserializeServer(JsonObject json, MinecraftServer server)
    {
        if (!deserializeSanctuaries(json, server))
            return false;

        if (!deserializeAvailable(json, server))
            return false;

        return true;
    }

    public enum ImprintSanctuaryResult {
        /** Player was able to imprint a Grove Sanctuary **/
        SUCCESS,

        /** Chunk is already part of a Grove Sanctuary **/
        ALREADY_EXISTS,

        /** Player already possesses <b>THIS</b> Grove Sanctuary **/
        ALREADY_HAS_GROVE,

        /** Player already possesses <b>A</b> Grove Sanctuary **/
        ALREADY_OWN_GROVE,

        /** Grove is abandoned and not accepting new imprints **/
        ABANDONED;
    }


    public record ClientGroveSanctuaryColorData(int sky, int grass, int foliage, int water)
    {
        public static final Codec<ClientGroveSanctuaryColorData> CODEC =
                Codec.INT_STREAM.comapFlatMap(
                        stream -> Util.decodeFixedLengthArray(stream, 4).map(values -> new ClientGroveSanctuaryColorData(values[0], values[1], values[2], values[3])),
                        data -> IntStream.of(new int[] { data.sky, data.grass, data.foliage, data.water })
                );

        public static final PacketCodec<RegistryByteBuf, ClientGroveSanctuaryColorData> PACKET_CODEC = PacketCodec.tuple(
                PacketCodecs.INTEGER, ClientGroveSanctuaryColorData::sky,
                PacketCodecs.INTEGER, ClientGroveSanctuaryColorData::grass,
                PacketCodecs.INTEGER, ClientGroveSanctuaryColorData::foliage,
                PacketCodecs.INTEGER, ClientGroveSanctuaryColorData::water,
                ClientGroveSanctuaryColorData::new);

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ClientGroveSanctuaryColorData other)
            {
                if(sky != other.sky) return false;
                if(grass != other.grass) return false;
                if(foliage != other.foliage) return false;
                if(water != other.water) return false;

                return true;
            }

            return false;
        }

        private String colorToString(int color)
        {
            if (color < 0) return "none";

            return Integer.toHexString(color).toUpperCase();
        }

        @Override
        public String toString() {
            return "[sky:" + colorToString(sky) + ", grass:" + colorToString(grass) + ", foliage:" + colorToString(foliage) + ", water:" + colorToString(water) + "]";
        }
    }



    @Environment(EnvType.CLIENT)
    private static final Map<RegistryKey<World>, Map<ChunkPos, ClientGroveSanctuaryColorData>> COLOR_MAP = new HashMap<>();

    @Environment(EnvType.CLIENT)
    public static @Nullable ClientGroveSanctuaryColorData getChunkColors(World world, ChunkPos pos)
    {
        if (COLOR_MAP.containsKey(world.getRegistryKey()))
        {
            Map<ChunkPos, ClientGroveSanctuaryColorData> chunks = COLOR_MAP.get(world.getRegistryKey());

            return chunks.get(pos);
        }

        return null;
    }

    @Environment(EnvType.CLIENT)
    public static void ClearChunkColors()
    {
        Groves.LOGGER.info("ClearChunkColors - called");
        COLOR_MAP.clear();
    }

    @Environment(EnvType.CLIENT)
    public static void SetChunkColors(RegistryKey<World> worldKey, ChunkPos pos, ClientGroveSanctuaryColorData colors)
    {
        Map<ChunkPos, ClientGroveSanctuaryColorData> chunks;

        if (COLOR_MAP.containsKey(worldKey))
        {
            chunks = COLOR_MAP.get(worldKey);
        }
        else
        {
            chunks = new HashMap<>();
            COLOR_MAP.put(worldKey, chunks);
        }

        chunks.put(pos, colors);
    }

    @Environment(EnvType.CLIENT)
    public static void SetChunkColors(RegistryKey<World> worldKey, List<ChunkPos> posList, ClientGroveSanctuaryColorData colors)
    {
        Map<ChunkPos, ClientGroveSanctuaryColorData> chunks;

        if (COLOR_MAP.containsKey(worldKey))
        {
            chunks = COLOR_MAP.get(worldKey);
        }
        else
        {
            chunks = new HashMap<>();
            COLOR_MAP.put(worldKey, chunks);
        }

        for(ChunkPos pos : posList) {
            chunks.put(pos, colors);
            Groves.LOGGER.info("SetChunksColors({}) {} -> {}", worldKey, pos, colors);
        }
    }

    @Environment(EnvType.CLIENT)
    public static void RemoveChunkColors(RegistryKey<World> worldKey, ChunkPos pos)
    {
        if (COLOR_MAP.containsKey(worldKey))
        {
            COLOR_MAP.get(worldKey).remove(pos);
        }
    }
}


