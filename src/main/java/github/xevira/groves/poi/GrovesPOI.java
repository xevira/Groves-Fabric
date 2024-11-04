package github.xevira.groves.poi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import github.xevira.groves.Groves;
import github.xevira.groves.ServerConfig;
import github.xevira.groves.network.*;
import github.xevira.groves.sanctuary.GroveAbilities;
import github.xevira.groves.sanctuary.GroveAbility;
import github.xevira.groves.screenhandler.GrovesSanctuaryScreenHandler;
import github.xevira.groves.util.ChunkHelper;
import github.xevira.groves.util.JSONHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.UserCache;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/** Manages the POIs for player-made Grove Sanctuaries **/
public class GrovesPOI {
    private static String JSON_SANCTUARIES = "sanctuaries";
    private static String JSON_AVAILABLE = "available";

    private static final List<GroveSanctuary> SANCTUARIES = new ArrayList<>();

    private static final Map<RegistryKey<World>, Set<ChunkPos>> AVAILABILITY = new HashMap<>();

    public static Optional<GroveSanctuary> getSanctuary(PlayerEntity player)
    {
        return SANCTUARIES.stream().filter(groveSanctuary -> groveSanctuary.isOwner(player)).findFirst();
    }

    public static boolean hasSanctuary(PlayerEntity player)
    {
        return SANCTUARIES.stream().anyMatch(groveSanctuary -> groveSanctuary.isOwner(player));
    }

    public static Optional<GroveSanctuary> getSanctuary(ServerWorld world, ChunkPos pos)
    {
        return SANCTUARIES.stream().filter(groveSanctuary -> groveSanctuary.contains(world, pos)).findFirst();
    }

    public static Optional<GroveSanctuary> getSanctuary(ServerWorld world, BlockPos pos)
    {
        return SANCTUARIES.stream().filter(groveSanctuary -> groveSanctuary.contains(world, new ChunkPos(pos))).findFirst();
    }

    public static Optional<GroveSanctuary> getSanctuaryAbandoned(ServerWorld world, BlockPos pos)
    {
        return SANCTUARIES.stream().filter(groveSanctuary -> groveSanctuary.contains(world, new ChunkPos(pos)) && groveSanctuary.isAbandoned()).findFirst();
    }

    public static boolean sanctuaryExists(ServerWorld world, ChunkPos pos)
    {
        return SANCTUARIES.stream().anyMatch(groveSanctuary -> groveSanctuary.contains(world, pos));
    }

    public static void claimAvailable(ServerWorld world, ChunkPos pos)
    {
        if (!AVAILABILITY.containsKey(world.getRegistryKey()))
            AVAILABILITY.put(world.getRegistryKey(), new HashSet<>());

        Set<ChunkPos> chunks = AVAILABILITY.get(world.getRegistryKey());

        chunks.add(pos);
    }

    public static void releaseAvailable(ServerWorld world, ChunkPos pos)
    {
        if (AVAILABILITY.containsKey(world.getRegistryKey()))
        {
            Set<ChunkPos> chunks = AVAILABILITY.get(world.getRegistryKey());

            chunks.remove(pos);
        }
    }

    public static boolean chunkAvailable(ServerWorld world, ChunkPos pos)
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

        // Set up the availability for the origin chunk
        sanctuary.calculateAvailable(chunkPos);

        // Default spawn point of the grove.
        sanctuary.setSpawnPoint(pos);

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
                        Optional<GroveSanctuary> sanctuary = GroveSanctuary.deserialize(item.getAsJsonObject(), server);

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
                if (!SANCTUARIES.isEmpty())
                    Groves.LOGGER.info("Loaded {} Grove Sanctuaries", SANCTUARIES.size());
                return true;
            }

            return false;
        }

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

    public static class GroveSanctuary implements ExtendedScreenHandlerFactory<GrovesSanctuaryScreenPayload> {
        public static final Text TITLE = Groves.text("gui", "groves");

        public static final Text INVALID_CHUNK_ERROR = Groves.text("error", "groves.claim_chunk.invalid");
        public static final Text ALREADY_OWN_ERROR = Groves.text("error", "groves.claim_chunk.own");
        public static final Text CLAIMED_ERROR = Groves.text("error", "groves.claim_chunk.claimed");

        public static final long MAX_SUNLIGHT_PER_CHUNK = 1000000L;

        private final MinecraftServer server;
        private UUID owner;
        private String ownerName;
        private String groveName;
        private boolean abandoned;

        private final ServerWorld world;
        private final GroveChunkData origin;
        private final List<GroveChunkData> groveChunks = new ArrayList<>();
        private final List<GroveFriend> groveFriends = new ArrayList<>();
        private final List<GroveAbility> groveAbilities = new ArrayList<>();
        private final Set<ChunkPos> availableChunks = new HashSet<>();
        private boolean enchanted;

        // Variable properties
        private long storedSunlight;
        private @Nullable BlockPos moonwell;
        private boolean chunkLoaded;
        private BlockPos spawnPoint;

        // Transient (will never save)
        private int lastServerTick;
        private int updatingTickChunk;
        private int lastTotalFoliage = -1;

        private final Set<ServerPlayerEntity> listeners = new HashSet<>();

        public GroveSanctuary(final MinecraftServer server, final PlayerEntity player, final ServerWorld world, final ChunkPos pos, final boolean enchanted)
        {
            this(server, player.getUuid(), player.getName().getString(), world, pos, enchanted);
        }

        public GroveSanctuary(final MinecraftServer server, final UUID owner, final String ownerName, final ServerWorld world, final ChunkPos pos, final boolean enchanted)
        {
            this(server, false, owner, ownerName, world, new GroveChunkData(world, pos), enchanted);
        }

        public GroveSanctuary(final MinecraftServer server, final ServerWorld world, final ChunkPos pos, final boolean enchanted)
        {
            this(server, true, null, "", world, new GroveChunkData(world, pos), enchanted);
        }

        public GroveSanctuary(final MinecraftServer server, final boolean abandoned, final UUID owner, final String ownerName, final ServerWorld world, final GroveChunkData data, final boolean enchanted)
        {
            this.server = server;
            this.owner = owner;
            this.ownerName = ownerName;
            this.abandoned = abandoned;
            this.groveName = "";
            this.world = world;
            this.origin = data;
            this.origin.setSanctuary(this);
            this.enchanted = enchanted;

            this.storedSunlight = 0;
            this.moonwell = null;
            this.chunkLoaded = false;

            this.lastServerTick = 0;
            this.updatingTickChunk = -1; // -1 == Origin
        }

        public void abandonSanctuary()
        {
            this.abandoned = true;
            this.owner = null;
            this.ownerName = "";
        }

        public boolean isAbandoned()
        {
            return this.abandoned;
        }

        public boolean reclaimSanctuary(PlayerEntity newOwner, boolean enchanted)
        {
            // TODO: add ways to not allow reclaiming

            this.owner = newOwner.getUuid();
            this.ownerName = newOwner.getName().getString();
            this.enchanted = enchanted;

            return true;
        }

        public void openUI(ServerPlayerEntity player)
        {
            player.openHandledScreen(this).ifPresent(syncid -> addListener(player));
        }

        public void addListener(ServerPlayerEntity player)
        {
            this.listeners.add(player);
        }

        public void removeListener(ServerPlayerEntity player)
        {
            this.listeners.remove(player);
        }

        public void sendListeners(CustomPayload payload)
        {
            this.listeners.stream()
                    .filter(listener -> listener.currentScreenHandler instanceof GrovesSanctuaryScreenHandler)
                    .forEach(listener -> ServerPlayNetworking.send(listener, payload));

            this.listeners.removeIf(listener -> !(listener.currentScreenHandler instanceof GrovesSanctuaryScreenHandler));
        }

        public void calculateAvailable(ChunkPos pos)
        {
            // First remove this chunk as available
            GrovesPOI.releaseAvailable(this.world, pos);
            this.availableChunks.remove(pos);
            sendListeners(new UpdateAvailableChunkPayload(pos, false));

            // Check each adjust chunk
            for(ChunkPos adj : ChunkHelper.getAdjacentChunks(pos))
            {
                Optional<GroveSanctuary> sanc = GrovesPOI.getSanctuary(this.world, adj);

                if (sanc.isEmpty() && isChunkValidForGrove(this.world, adj, this.enchanted))
                {
                    GrovesPOI.claimAvailable(this.world, adj);
                    this.availableChunks.add(adj);
                    sendListeners(new UpdateAvailableChunkPayload(adj, true));
                }
            }
        }

        // Check to make sure the chunk is *still* in the AVAILABILITY map
        private void checkAvailable()
        {
            for(ChunkPos pos : this.availableChunks)
            {
                if (!GrovesPOI.chunkAvailable(this.world, pos))
                {
                    this.availableChunks.remove(pos);
                    sendListeners(new UpdateAvailableChunkPayload(pos, false));
                }
            }
        }

        private void resetAvailable()
        {
            // Release chunks
            this.availableChunks.forEach(pos -> GrovesPOI.releaseAvailable(this.world, pos));

            // Recalculate edges
            this.availableChunks.clear();
            calculateAvailable(this.origin.chunkPos);
            this.groveChunks.forEach(chunk -> calculateAvailable(chunk.chunkPos));

            sendListeners(new ResetAvailableChunksPayload(this.availableChunks));
        }

        public MinecraftServer getServer()
        {
            return this.server;
        }

        public ServerPlayerEntity getOwnerPlayer()
        {
            if (this.abandoned) return null;
            return this.server.getPlayerManager().getPlayer(this.owner);
        }

        public boolean isOwnerInSanctuary()
        {
            ServerPlayerEntity owner = getOwnerPlayer();
            if (owner == null) return false;

            return contains(owner.getBlockPos());
        }

        public @Nullable UUID getOwner()
        {
            return this.owner;
        }

        public String getOwnerName()
        {
            return this.ownerName;
        }

        public boolean isOwner(@NotNull PlayerEntity player)
        {
            return this.owner.equals(player.getUuid());
        }

        public void setGroveName(@NotNull String name)
        {
            this.groveName = name;
        }

        public String getGroveName()
        {
            return this.groveName;
        }

        public long getStoredSunlight()
        {
            return this.storedSunlight;
        }

        public void setStoredSunlight(long sunlight)
        {
            this.storedSunlight = sunlight;
        }

        public @Nullable BlockPos getMoonwell()
        {
            return this.moonwell;
        }

        public boolean hasMoonwell()
        {
            return this.moonwell != null;
        }

        public void setMoonwell(BlockPos pos)
        {
            this.moonwell = pos.mutableCopy();
            sendListeners(new UpdateMoonwellPayload(this.moonwell));
        }

        public void clearMoonwell()
        {
            this.moonwell = null;
            sendListeners(new UpdateMoonwellPayload(this.moonwell));
        }

        /**
         *  <p>Specifies the total sunlight the Grove Sanctuary can contain.</p>
         **/
        public long getMaxStoredSunlight()
        {
            return MAX_SUNLIGHT_PER_CHUNK * (groveChunks.size() + 1 /* for the origin chunk */);
        }

        /**
         *  <p>Accumulates sunlight for the Grove Sanctuary.  Supplying a negative value will drain the reservoir.</p>
         **/
        public void addSunlight(long sunlight)
        {
            this.storedSunlight = MathHelper.clamp(this.storedSunlight + sunlight, 0, getMaxStoredSunlight());
            sendListeners(new UpdateSunlightPayload(this.storedSunlight));
        }

        public void useSunlight(long sunlight)
        {
            this.storedSunlight = MathHelper.clamp(this.storedSunlight - sunlight, 0, getMaxStoredSunlight());
            sendListeners(new UpdateSunlightPayload(this.storedSunlight));
        }

        public enum SetSpawnPointResult
        {
            SUCCESS,
            NOT_GROVE,
            AIR;
        }

        private @Nullable BlockPos placeOnLand(BlockPos pos)
        {
            pos = pos.down();

            while(pos.getY() >= this.world.getBottomY())
            {
                BlockState state = this.world.getBlockState(pos);

                if (!state.isAir()) return pos;

                pos = pos.down();
            }

            return null;
        }

        public SetSpawnPointResult setSpawnPoint(BlockPos pos)
        {
            if (!contains(pos)) return SetSpawnPointResult.NOT_GROVE;

            pos = placeOnLand(pos);
            if (pos == null) return SetSpawnPointResult.AIR;

            this.spawnPoint = pos.up();
            return SetSpawnPointResult.SUCCESS;
        }

        public BlockPos getSpawnPoint()
        {
            return this.spawnPoint;
        }

        public boolean sameWorld(@NotNull ServerWorld world)
        {
            return this.world == world;
        }

        public int totalChunks()
        {
            return this.groveChunks.size() + 1;
        }

        /**
         * Adds the specified chunk to the Grove Sanctuary.
         *
         **/
        public boolean addChunk(ChunkPos pos)
        {
            if(groveChunks.stream().noneMatch(chunk -> chunk.isChunk(pos))) {
                groveChunks.add(new GroveChunkData(this, this.world, pos));
                calculateAvailable(pos);
                return true;
            }

            return false;
        }

        public void claimChunk(ServerPlayerEntity player, ChunkPos pos)
        {
            Optional<GroveSanctuary> sanc = GrovesPOI.getSanctuary(this.world, pos);

            // Not part of a sanctuary
            if (sanc.isEmpty())
            {
                if (GrovesPOI.isChunkValidForGrove(this.world, pos, this.enchanted))
                {
                    long total = this.totalChunks();

                    long cost = total * total * ServerConfig.getBaseCostClaimChunk();

                    if (getStoredSunlight() < cost)
                    {
                        ServerPlayNetworking.send(player, new ClaimChunkResponsePayload(pos, false, Groves.text("error", "groves.claim_chunk.cost", cost)));
                    }
                    else {
                        addChunk(pos);
                        useSunlight(cost);
                        sendListeners(new ClaimChunkResponsePayload(pos, true, Text.empty()));
                    }
                }
                else
                    ServerPlayNetworking.send(player, new ClaimChunkResponsePayload(pos, false, INVALID_CHUNK_ERROR));
            }
            else if (sanc.get() == this)
                ServerPlayNetworking.send(player, new ClaimChunkResponsePayload(pos, false, ALREADY_OWN_ERROR));
            else
                ServerPlayNetworking.send(player, new ClaimChunkResponsePayload(pos, false, CLAIMED_ERROR));
        }

        /**
         *  <p>Removes the specified chunk from the Grove Sanctuary.</p>
         *
         *  <p>NOTE: The origin chunk cannot be removed without removing the entire Sanctuary.</p>
         **/
        public boolean removeChunk(ChunkPos pos)
        {
            if (groveChunks.removeIf(chunk -> chunk.isChunk(pos))) {

                if (this.updatingTickChunk >= this.groveChunks.size())
                    this.updatingTickChunk = -1;

                resetAvailable();
                return true;
            }

            return false;
        }

        public Optional<GroveChunkData> getChunk(ChunkPos pos)
        {
            if (isOrigin(pos)) return Optional.of(this.origin);

            return this.groveChunks.stream().filter(chunk -> chunk.isChunk(pos)).findFirst();
        }

        /** Indicates whether the specified chunk is the origin point of the Grove Sanctuary **/
        public boolean isOrigin(ChunkPos pos)
        {
            return this.origin.isChunk(pos);
        }

        /** Indicates whether the Grove Sanctuary contains the specified chunk **/
        public boolean contains(ChunkPos pos)
        {
            if (isOrigin(pos)) return true;

            return groveChunks.stream().anyMatch(chunk -> chunk.isChunk(pos));
        }

        public boolean contains(ChunkPos pos1, ChunkPos pos2)
        {
            if (isOrigin(pos1)) return true;
            if (isOrigin(pos2)) return true;

            return groveChunks.stream().anyMatch(chunk -> chunk.isChunk(pos1) || chunk.isChunk(pos2));
        }

        public boolean contains(BlockPos pos)
        {
            ChunkPos chunkPos = new ChunkPos(pos);
            return contains(chunkPos);
        }

        public boolean contains(BlockPos pos1, BlockPos pos2)
        {
            ChunkPos chunkPos1 = new ChunkPos(pos1);
            ChunkPos chunkPos2 = new ChunkPos(pos2);
            return contains(chunkPos1, chunkPos2);
        }

        /** Indicates whether the Grove Sanctuary is in the specified world and contains the specified chunk **/
        public boolean contains(ServerWorld world, ChunkPos pos)
        {
            if (!sameWorld(world)) return false;

            return contains(pos);
        }

        public void addFriend(ServerPlayerEntity player, String name)
        {
            UserCache users = this.server.getUserCache();

            if (users != null)
            {
                GameProfile profile = users.findByName(name).orElse(null);

                if (profile != null)
                {
                    if (this.groveFriends.stream().anyMatch(friend -> friend.id().equals(profile.getId())))
                    {
                        ServerPlayNetworking.send(player, new AddFriendResponsePayload(name, Text.literal("Player is already a friend.")));
                    }
                    else
                    {
                        this.groveFriends.add(new GroveFriend(profile));
                        // Send to everyone looking at the UI, not just whoever is adding the friend
                        sendListeners(new AddFriendResponsePayload(profile.getId(), profile.getName()));
                    }
                }
                else
                {
                    ServerPlayNetworking.send(player, new AddFriendResponsePayload(name, Text.literal("Player not found.")));
                }

            }
        }

        public void removeFriend(ServerPlayerEntity player, UUID uuid)
        {
            Groves.LOGGER.info("SERVER.removeFriend: {}", uuid);

            if (this.groveFriends.removeIf(friend -> friend.id().equals(uuid)))
            {
                Groves.LOGGER.info("SERVER.removeFriend: {} removed", uuid);
                sendListeners(new RemoveFriendResponsePayload(uuid));
            }
            else
            {
                ServerPlayNetworking.send(player, new RemoveFriendResponsePayload(Text.literal("Player not in friend list.")));
            }
        }


        public Optional<GroveAbility> getAbility(int id)
        {
            return this.groveAbilities.stream().filter(ability -> ability.getId() == id).findFirst();
        }

        public Optional<GroveAbility> getAbility(String name)
        {
            return this.groveAbilities.stream().filter(ability -> ability.getName().equalsIgnoreCase(name)).findFirst();
        }

        public boolean hasAbility(String name)
        {
            return this.groveAbilities.stream().anyMatch(ability -> ability.getName().equalsIgnoreCase(name));
        }

        public void installAbility(GroveAbility prototype)
        {
            this.groveAbilities.add(prototype.getConstructor().get());
        }

        private boolean isOwnerOnline(MinecraftServer server)
        {
            List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();

            return players.stream().anyMatch(player -> this.owner.equals(player.getUuid()));
        }

        private GroveChunkData getUpdatingChunk()
        {
            if (this.updatingTickChunk < 0) return this.origin;

            return this.groveChunks.get(this.updatingTickChunk);
        }

        private void processFoliage()
        {
            // Check if the owner is online
            if (!isOwnerOnline(this.server)) return;

            // World must have daylight
            if (!this.world.isDay()) return;

            // Step 1: update foliage count in currently selected chunk
            GroveChunkData data = getUpdatingChunk();
            if (data.onServerTick(this.server))
            {
                // Done with this chunk, move onto the next one;
                if (++this.updatingTickChunk >= this.groveChunks.size())
                    this.updatingTickChunk = -1;    // Reset back to the origin chunk
            }

            // Step 2: total number of (cached) foliage in the grove
            int totalFoliage = groveChunks.stream().map(chunk -> chunk.foliage).reduce(this.origin.foliage, Integer::sum);

            // TODO: Configuration option for the rates.
            float powerRating = this.world.isRaining() ? 0.05f : 0.15f;

            // TODO: Configuration option for the enchanted rate bonus
            if (this.enchanted) powerRating *= 1.5f;

            // Step 3: convert foliage count into solar power
            long solarPower = (long)(totalFoliage * powerRating);

            if (solarPower > 0)
                addSunlight(solarPower);

            if (totalFoliage != lastTotalFoliage)
            {
                lastTotalFoliage = totalFoliage;
                sendListeners(new UpdateTotalFoliagePayload(totalFoliage));
            }
        }

        private void processAbilities()
        {
            this.groveAbilities.forEach(ability -> {
                if (ability.isActive())
                {
                    if (!ability.isEnabled())
                    {
                        // Ability was disabled, so deactivate it
                        ability.setActive(false);
                        ability.onDeactivate(this.server, this, null);
                    }
                    else if (ability.onServerTick(this.server, this)) {
                        if (ability.autoDeactivate()) {
                            ability.setActive(false);
                            ability.onDeactivate(this.server, this, null);
                        }
                    }
                }
            });
        }

        /** Server Tick event used to generate passive sunlight **/
        public void onEndServerTick(MinecraftServer server)
        {
            if (this.lastServerTick-- > 0)
                return;

            this.lastServerTick = 20;   // Once a second

            checkAvailable();

            processFoliage();
            processAbilities();
        }

        public void setChunkLoading(boolean state)
        {
            if (this.origin.chunkLoad)
                this.origin.setChunkLoad(state);

            this.groveChunks.stream().filter(chunk -> chunk.chunkLoad).forEach(chunk -> chunk.setChunkLoad(state));
        }

        public void setChunkLoadingForChunk(ChunkPos pos, boolean state)
        {
            Optional<GroveChunkData> data = getChunk(pos);

            data.ifPresent(chunk -> chunk.keepLoaded(state));
        }

        public JsonObject serialize(MinecraftServer server)
        {
            JsonObject json = new JsonObject();

            if (this.abandoned)
            {
                json.add("abandoned", new JsonPrimitive(true));
            }
            else {
                json.add("owner", new JsonPrimitive(this.owner.toString()));
                json.add("ownerName", new JsonPrimitive(this.ownerName));
            }

            json.add("groveName", new JsonPrimitive(this.groveName));

            json.add("world", new JsonPrimitive(this.world.getRegistryKey().getValue().toString()));
            json.add("origin", this.origin.serializeServer(server));

            json.add("enchanted", new JsonPrimitive(this.enchanted));
            json.add("chunkLoaded", new JsonPrimitive(this.chunkLoaded));

            json.add("spawnPoint", JSONHelper.BlockPosToJson(this.spawnPoint));

            if (this.moonwell != null)
                json.add("moonwell", JSONHelper.BlockPosToJson(this.moonwell));

            JsonArray chunks = new JsonArray();
            this.groveChunks.stream().map(chunk -> chunk.serializeServer(server)).forEach(chunks::add);
            json.add("chunks", chunks);

            JsonArray available = new JsonArray();
            this.availableChunks.stream().map(JSONHelper::ChunkPosToJson).forEach(available::add);
            json.add("available", available);

            JsonArray friends = new JsonArray();
            this.groveFriends.stream().map(GroveFriend::serialize).forEach(friends::add);
            json.add("friends", friends);

            JsonArray abilities = new JsonArray();
            this.groveAbilities.stream().map(GroveAbility::serialize).forEach(abilities::add);
            json.add("abilities", abilities);

            json.add("sunlight", new JsonPrimitive(this.storedSunlight));

            return json;
        }

        private static List<GroveChunkData> deserializeChunks(JsonObject json, MinecraftServer server, ServerWorld world)
        {
            List<GroveChunkData> chunks = new ArrayList<>();
            if (json.has("chunks"))
            {
                JsonElement element = json.get("chunks");

                if (element.isJsonArray())
                {
                    JsonArray array = element.getAsJsonArray();

                    for(JsonElement item : array)
                    {
                        if (item.isJsonObject())
                        {
                            Optional<GroveChunkData> data = GroveChunkData.deserializeServer(item.getAsJsonObject(), server, world);

                            data.ifPresent(chunks::add);
                        }
                    }
                }
            }

            return chunks;
        }

        private static Set<ChunkPos> deserializeAvailable(JsonObject json)
        {
            Set<ChunkPos> available = new HashSet<>();
            if (json.has("available"))
            {
                JsonElement element = json.get("available");

                if (element.isJsonArray())
                {
                    JsonArray array = element.getAsJsonArray();

                    for(JsonElement item : array)
                    {
                        if (item.isJsonObject())
                        {
                            Optional<ChunkPos> pos = JSONHelper.JsonToChunkPos(item.getAsJsonObject());

                            pos.ifPresent(available::add);
                        }
                    }
                }
            }

            return available;
        }

        private static List<GroveFriend> deserializeFriends(JsonObject json)
        {
            List<GroveFriend> friends = new ArrayList<>();
            if (json.has("friends"))
            {
                JsonElement element = json.get("friends");

                if (element.isJsonArray())
                {
                    JsonArray array = element.getAsJsonArray();

                    for(JsonElement item : array)
                    {
                        if (item.isJsonObject())
                        {
                            Optional<GroveFriend> friend = GroveFriend.deserialize(item.getAsJsonObject());

                            friend.ifPresent(friends::add);
                        }
                    }
                }
            }

            return friends;
        }

        private static List<GroveAbility> deserializeAbilities(JsonObject json)
        {
            List<GroveAbility> abilities = new ArrayList<>();
            if (json.has("abilities"))
            {
                JsonElement element = json.get("abilities");

                if (element.isJsonArray())
                {
                    JsonArray array = element.getAsJsonArray();

                    for(JsonElement item : array)
                    {
                        if (item.isJsonObject())
                        {
                            Optional<GroveAbility> ability = GroveAbility.deserialize(item.getAsJsonObject());

                            ability.ifPresent(abilities::add);
                        }
                    }
                }
            }

            return abilities;
        }


        public static Optional<GroveSanctuary> deserialize(JsonObject json, MinecraftServer server)
        {
            boolean abandoned = JSONHelper.getBoolean(json, "abandoned", false);

            UUID owner;
            String ownerName;

            if (abandoned)
            {
                owner = null;
                ownerName = "";
            }
            else {
                owner = JSONHelper.getUUID(json, "owner").orElse(null);
                if (owner == null) {
                    Groves.LOGGER.error("Invalid owner in Groves Sanctuary");
                    return Optional.empty();
                }

                ownerName = JSONHelper.getString(json, "ownerName");
                if (ownerName == null) {
                    Groves.LOGGER.error("Invalid owner name in Groves Sanctuary");
                    return Optional.empty();
                }
            }

            String groveName = JSONHelper.getString(json, "groveName", "");

            String worldId = JSONHelper.getString(json, "world");
            if (worldId == null) {
                Groves.LOGGER.error("Missing world in Groves Sanctuary");
                return Optional.empty();
            }

            RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(worldId));
            ServerWorld world = server.getWorld(worldKey);
            if (world == null) {
                Groves.LOGGER.error("Invalid world '{}' in Groves Sanctuary", worldId);
                return Optional.empty();
            }

            Optional<JsonObject> originJson = JSONHelper.getObject(json, "origin");
            if (originJson.isEmpty()) {
                Groves.LOGGER.error("Invalid origin chunk in Groves Sanctuary");
                return Optional.empty();
            }

            Optional<GroveChunkData> origin = GroveChunkData.deserializeServer(originJson.get(), server, world);
            if (origin.isEmpty()) {
                Groves.LOGGER.error("Invalid origin chunk in Grove Sanctuary");
                return Optional.empty();
            }

            Optional<BlockPos> spawnPoint = JSONHelper.JsonToBlockPos(json, "spawnPoint");
            if (spawnPoint.isEmpty())
            {
                Groves.LOGGER.error("Invalid spawn point in Grove Sanctuary");
                return Optional.empty();
            }

            Optional<Boolean> enchanted = JSONHelper.getBoolean(json, "enchanted");
            Optional<Boolean> chunkLoaded = JSONHelper.getBoolean(json, "chunkLoaded");
            Optional<BlockPos> moonwell = JSONHelper.JsonToBlockPos(json, "moonwell");

            // If it is empty, it will reset to 0.
            Optional<Long> storedSunlight = JSONHelper.getLong(json, "sunlight");

            List<GroveChunkData> chunks = deserializeChunks(json, server, world);
            Set<ChunkPos> available = deserializeAvailable(json);
            List<GroveFriend> friends = deserializeFriends(json);
            List<GroveAbility> abilities = deserializeAbilities(json);

            GroveSanctuary sanctuary = new GroveSanctuary(server, abandoned, owner, ownerName, world, origin.get(), enchanted.orElse(false));

            sanctuary.groveName = groveName;

            spawnPoint.ifPresent(sanctuary::setSpawnPoint);
            sanctuary.groveChunks.addAll(chunks);
            sanctuary.groveFriends.addAll(friends);
            sanctuary.groveAbilities.addAll(abilities);
            sanctuary.availableChunks.addAll(available);

            chunkLoaded.ifPresent(aBoolean -> sanctuary.chunkLoaded = aBoolean);
            storedSunlight.ifPresent(sunlight -> sanctuary.storedSunlight = sunlight);
            moonwell.ifPresent(blockPos -> sanctuary.moonwell = blockPos);

            return Optional.of(sanctuary);
        }

        public ClientGroveSanctuary createClientData()
        {
            ClientGroveSanctuary sanctuary = new ClientGroveSanctuary(this.abandoned, new ClientGroveSanctuary.ChunkData(this.origin.chunkPos, this.origin.chunkLoad), this.enchanted);
            sanctuary.setGroveName(this.groveName);
            sanctuary.setSpawnPoint(this.spawnPoint);
            sanctuary.setChunkLoading(this.chunkLoaded);

            this.groveChunks.stream().map(GroveChunkData::toClientData).forEach(sanctuary::addChunk);
            this.groveFriends.stream().map(GroveFriend::toGameProfile).forEach(sanctuary.groveFriends::add);

            sanctuary.setFoliage(groveChunks.stream().map(chunk -> chunk.foliage).reduce(this.origin.foliage, Integer::sum));
            sanctuary.setStoredSunlight(this.storedSunlight);
            sanctuary.setMoonwell(this.moonwell);

            sanctuary.groveAbilities.addAll(this.groveAbilities);
            sanctuary.setAvailableChunks(this.availableChunks);

            return sanctuary;
        }

        @Override
        public GrovesSanctuaryScreenPayload getScreenOpeningData(ServerPlayerEntity serverPlayerEntity) {
            return new GrovesSanctuaryScreenPayload(createClientData());
        }

        @Override
        public Text getDisplayName() {
            return TITLE;
        }

        @Override
        public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
            return new GrovesSanctuaryScreenHandler(syncId, playerInventory, createClientData());
        }

        public static class GroveChunkData {
            private GroveSanctuary sanctuary;
            public final ServerWorld world;
            public final ChunkPos chunkPos;

            public int foliage;
            public final int[] foliageRows = new int[16];

            public boolean chunkLoad;

            private int updatingRow;

            public GroveChunkData(ServerWorld world, ChunkPos chunk)
            {
                this.world = world;
                this.chunkPos = chunk;
                this.foliage = 0;
                Arrays.fill(this.foliageRows, 0);
                this.chunkLoad = false;

                this.updatingRow = 0;
            }

            public GroveChunkData(GroveSanctuary sanctuary, ServerWorld world, ChunkPos chunk)
            {
                this(world, chunk);
                this.sanctuary = sanctuary;
            }

            public ClientGroveSanctuary.ChunkData toClientData()
            {
                return new ClientGroveSanctuary.ChunkData(this.chunkPos, this.chunkLoad);
            }

            public void setSanctuary(GroveSanctuary sanctuary)
            {
                this.sanctuary = sanctuary;
            }

            public void keepLoaded(boolean enable)
            {
                if (enable)
                    this.world.setChunkForced(this.chunkPos.x, this.chunkPos.z, true);
                else if (this.chunkLoad)
                    this.world.setChunkForced(this.chunkPos.x, this.chunkPos.z, false);
                this.chunkLoad = enable;
            }

            public void setChunkLoad(boolean enable)
            {
                this.world.setChunkForced(this.chunkPos.x, this.chunkPos.z, enable);
            }

            public int getFoliage()
            {
                return this.foliage;
            }

            public boolean isChunk(ChunkPos chunk)
            {
                return this.chunkPos.equals(chunk);
            }

            /** Finds the topmost non-air non-liquid non-transparent block.  This might need to be updated if this is too slow **/
            @SuppressWarnings("deprecation")
            private BlockPos.Mutable getTopMostBlock(Chunk chunk, int x, int z, int topy)
            {
                BlockPos.Mutable mutable = new BlockPos.Mutable(x, topy, z);
                for(int y = topy; y >= chunk.getBottomY(); y--)
                {
                    mutable.set(x, y, z);
                    BlockState state = chunk.getBlockState(mutable);

                    if (!state.isAir() && !state.isLiquid() && !state.isTransparent())
                        return mutable;
                }

                mutable.set(x, chunk.getBottomY() - 1, z);
                return mutable;
            }

            private boolean isValidLeaf(BlockState state)
            {
                // Not even considered a leaf
                if (!state.isIn(BlockTags.LEAVES) && !(state.getBlock() instanceof LeavesBlock))
                    return false;

                // Only naturally generated leaves count.
                return !state.contains(Properties.PERSISTENT) || !state.get(Properties.PERSISTENT);
            }


            /**
             *  Scans the chunk, one row at a time, counting valid leaf blocks
             *  {@return true if this chunk is done scanning }
             **/
            @SuppressWarnings("removal")
            public boolean onServerTick(MinecraftServer server)
            {
                if (world.isChunkLoaded(chunkPos.x, chunkPos.z)) {

                    int bx = chunkPos.x * 16;
                    int bz = chunkPos.z * 16;

                    Chunk chunk = world.getChunk(this.chunkPos.x, this.chunkPos.z);

                    int topY = chunk.getHighestNonEmptySectionYOffset() + 16;

                    int leaves = 0;
                    int x = this.updatingRow;
                    for (int z = 0; z < 16; z++) {
                        BlockPos pos = getTopMostBlock(chunk, bx + x, bz + z, topY);
                        BlockState state = world.getBlockState(pos);

                        if (isValidLeaf(state))
                            ++leaves;
                    }
                    // Subtract the old value and add the new value
                    this.foliage -= this.foliageRows[this.updatingRow];
                    this.foliage += leaves;
                    this.foliageRows[this.updatingRow] = leaves;

                    if (++this.updatingRow >= 16)
                    {
                        this.updatingRow = 0;
                        return true;
                    }
                }

                return false;
            }

            public JsonObject serializeServer(MinecraftServer server)
            {
                JsonObject json = new JsonObject();

                json.add("chunk", JSONHelper.ChunkPosToJson(this.chunkPos));
                json.add("foliage", new JsonPrimitive(this.foliage));

                JsonArray array = new JsonArray(16);
                Arrays.stream(this.foliageRows).mapToObj(JsonPrimitive::new).forEachOrdered(array::add);
                json.add("foliageRows", array);

                if (this.chunkLoad)
                    json.add("chunkLoad", new JsonPrimitive(true));

                return json;
            }

            public static Optional<GroveChunkData> deserializeServer(JsonObject json, MinecraftServer server, ServerWorld world)
            {
                Optional<ChunkPos> chunk = JSONHelper.JsonToChunkPos(json, "chunk");
                Optional<Integer> foliage = JSONHelper.getInt(json, "foliage");
                Optional<Boolean> chunkLoad = JSONHelper.getBoolean(json, "chunkLoad");
                int[] foliageRows = JSONHelper.getIntArray(json, "foliageRows", 16);

                if (foliage.isPresent() && foliageRows.length == 16)
                {
                    int sum = Arrays.stream(foliageRows).sum();
                    // If they don't match, use the sum of the rows.
                    if (foliage.get() != sum) {
                        foliage = Optional.of(sum);
                    }
                }

                if (chunk.isPresent() && foliage.isPresent() && foliageRows.length == 16)
                {
                    GroveChunkData data = new GroveChunkData(world, chunk.get());
                    data.foliage = foliage.get();
                    for(int i = 0; i < foliageRows.length; i++) {
                        data.foliageRows[i] = foliageRows[i];
                    }
                    data.chunkLoad = chunkLoad.isPresent() && chunkLoad.get();

                    return Optional.of(data);
                }

                return Optional.empty();
            }
        }
    }


    public record GroveFriend(UUID id, String name) {
        public static final PacketCodec<RegistryByteBuf, GroveFriend> PACKET_CODEC = new PacketCodec<RegistryByteBuf, GroveFriend>() {
            @Override
            public GroveFriend decode(RegistryByteBuf buf) {
                UUID id = Uuids.PACKET_CODEC.decode(buf);
                String name = PacketCodecs.STRING.decode(buf);

                return new GroveFriend(id, name);
            }

            @Override
            public void encode(RegistryByteBuf buf, GroveFriend value) {
                Uuids.PACKET_CODEC.encode(buf, value.id());
                PacketCodecs.STRING.encode(buf, value.name());
            }
        };


        public GroveFriend(GameProfile profile)
        {
            this(profile.getId(), profile.getName());
        }

        public GroveFriend(PlayerEntity friend)
        {
            this(friend.getGameProfile());
        }

        public boolean isFriend(PlayerEntity friend)
        {
            return id.equals(friend.getUuid());
        }

        public GameProfile toGameProfile()
        {
            return new GameProfile(this.id, this.name);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof GroveFriend friend)
            {
                if (!friend.id.equals(this.id)) return false;
                if (!friend.name.equals(this.name)) return false;

                return true;
            }

            if (obj instanceof UUID uuid)
            {
                return this.id.equals(uuid);
            }

            return false;
        }

        public JsonObject serialize()
        {
            JsonObject json = new JsonObject();

            json.add("name", new JsonPrimitive(this.name));
            json.add("id", new JsonPrimitive(this.id.toString()));

            return json;
        }

        public static Optional<GroveFriend> deserialize(JsonObject json)
        {
            Optional<UUID> id = JSONHelper.getUUID(json, "id");
            String name = JSONHelper.getString(json, "name");

            if (id.isPresent() && name != null)
                return Optional.of(new GroveFriend(id.get(), name));

            return Optional.empty();
        }
    }

    public static class ClientGroveSanctuary
    {
        public static final PacketCodec<RegistryByteBuf, ClientGroveSanctuary> PACKET_CODEC = new PacketCodec<RegistryByteBuf, ClientGroveSanctuary>() {
            @Override
            public ClientGroveSanctuary decode(RegistryByteBuf buf) {
                boolean abandoned = PacketCodecs.BOOL.decode(buf);
                String groveName = PacketCodecs.STRING.decode(buf);
                ChunkData origin = ChunkData.PACKET_CODEC.decode(buf);
                boolean enchanted = PacketCodecs.BOOL.decode(buf);
                boolean chunkLoading = PacketCodecs.BOOL.decode(buf);

                BlockPos spawnPoint = BlockPos.PACKET_CODEC.decode(buf);

                ClientGroveSanctuary sanctuary = new ClientGroveSanctuary(abandoned, origin, enchanted);

                sanctuary.setGroveName(groveName);
                sanctuary.setSpawnPoint(spawnPoint);
                sanctuary.setChunkLoading(chunkLoading);

                int chunks = PacketCodecs.INTEGER.decode(buf);
                for(int i = 0; i < chunks; i++)
                    sanctuary.addChunk(ChunkData.PACKET_CODEC.decode(buf));

                int available = PacketCodecs.INTEGER.decode(buf);
                for(int i = 0; i < available; i++)
                    sanctuary.addAvailable(ChunkPos.PACKET_CODEC.decode(buf));

                sanctuary.setFoliage(PacketCodecs.INTEGER.decode(buf));
                sanctuary.setStoredSunlight(PacketCodecs.LONG.decode(buf));

                if (PacketCodecs.BOOL.decode(buf))
                    sanctuary.setMoonwell(BlockPos.PACKET_CODEC.decode(buf));

                int friends = PacketCodecs.INTEGER.decode(buf);
                for(int i = 0; i < friends; i++) {
                    UUID friendID = Uuids.PACKET_CODEC.decode(buf);
                    String friendName = PacketCodecs.STRING.decode(buf);
                    sanctuary.addFriend(new GameProfile(friendID, friendName));
                }

                int abilities = PacketCodecs.INTEGER.decode(buf);
                for(int i = 0; i < abilities; i++) {
                    GroveAbility ability = GroveAbility.PACKET_CODEC.decode(buf);
                    if (ability != null)
                        sanctuary.addAbility(ability);
                }

                return sanctuary;
            }

            @Override
            public void encode(RegistryByteBuf buf, ClientGroveSanctuary value) {
                PacketCodecs.BOOL.encode(buf, value.abandoned);
                PacketCodecs.STRING.encode(buf, value.groveName);
                ChunkData.PACKET_CODEC.encode(buf, value.origin);
                PacketCodecs.BOOL.encode(buf, value.enchanted);
                PacketCodecs.BOOL.encode(buf, value.chunkLoading);

                BlockPos.PACKET_CODEC.encode(buf, value.spawnPoint);

                PacketCodecs.INTEGER.encode(buf, value.groveChunks.size());
                value.groveChunks.forEach(chunk -> ChunkData.PACKET_CODEC.encode(buf, chunk));

                PacketCodecs.INTEGER.encode(buf, value.availableChunks.size());
                value.availableChunks.forEach(chunk -> ChunkPos.PACKET_CODEC.encode(buf, chunk));

                PacketCodecs.INTEGER.encode(buf, value.foliage);
                PacketCodecs.LONG.encode(buf, value.storedSunlight);

                BlockPos well = value.getMoonwell();
                if (well != null) {
                    PacketCodecs.BOOL.encode(buf, true);
                    BlockPos.PACKET_CODEC.encode(buf, well);
                }
                else
                    PacketCodecs.BOOL.encode(buf, false);

                PacketCodecs.INTEGER.encode(buf, value.groveFriends.size());
                value.groveFriends.forEach(friend -> {
                    if (friend.getId() != null && friend.getName() != null)
                    {
                        Uuids.PACKET_CODEC.encode(buf, friend.getId());
                        PacketCodecs.STRING.encode(buf, friend.getName());
                    }
                });

                PacketCodecs.INTEGER.encode(buf, value.groveAbilities.size());
                value.groveAbilities.forEach(ability -> GroveAbility.PACKET_CODEC.encode(buf, ability));
            }
        };

        private @Nullable GroveSanctuary sanctuary;

        private boolean abandoned;
        private String groveName;
        private ChunkData origin;
        private BlockPos spawnPoint;
        private boolean enchanted;
        private boolean chunkLoading;

        private final List<ChunkData> groveChunks = new ArrayList<>();
        private int foliage;

        private long storedSunlight;
        private BlockPos moonwell;

        private final List<GameProfile> groveFriends = new ArrayList<>();
        private final List<GroveAbility> groveAbilities = new ArrayList<>();
        private final Set<ChunkPos> availableChunks = new HashSet<>();

        public ClientGroveSanctuary(boolean abandoned, ChunkData origin, boolean enchanted)
        {
            this.abandoned = abandoned;
            this.origin = origin;
            this.enchanted = enchanted;
            this.chunkLoading = false;
        }

        public void setAbandoned(boolean abandoned)
        {
            this.abandoned = abandoned;
        }

        public boolean isAbandoned()
        {
            return this.abandoned;
        }

        public void setSanctuary(@Nullable GroveSanctuary sanctuary)
        {
            this.sanctuary = sanctuary;
        }

        public @Nullable GroveSanctuary getSanctuary()
        {
            return this.sanctuary;
        }

        public String getGroveName()
        {
            return this.groveName;
        }

        public void setGroveName(@NotNull String name)
        {
            this.groveName = name;
        }

        public void setSpawnPoint(BlockPos pos)
        {
            this.spawnPoint = pos;
        }

        public BlockPos getSpawnPoint()
        {
            return this.spawnPoint;
        }

        public void setChunkLoading(boolean loading)
        {
            this.chunkLoading = loading;
        }

        public List<GameProfile> getFriends()
        {
            return this.groveFriends;
        }

        public void addFriend(@NotNull UUID uuid, @NotNull String name)
        {
            GroveFriend friend = new GroveFriend(uuid, name);
            addFriend(friend);
        }

        public void addFriend(GroveFriend friend)
        {
            if (this.groveFriends.stream().noneMatch(f -> friend.id().equals(f.getId())))
                this.groveFriends.add(friend.toGameProfile());
        }

        public void addFriend(GameProfile profile)
        {
            if (profile.getId() == null) return;

            if (this.groveFriends.stream().noneMatch(f -> profile.getId().equals(f.getId())))
                this.groveFriends.add(profile);
        }

        public void removeFriend(@NotNull UUID uuid)
        {
            Groves.LOGGER.info("CLIENT: Removing Friend: {}", uuid);
            if (this.groveFriends.removeIf(friend -> uuid.equals(friend.getId())))
                Groves.LOGGER.info("CLIENT: Friend removed");
        }

        public void removeFriend(int index)
        {
            if (index >= 0 && index < this.groveFriends.size())
                this.groveFriends.remove(index);
        }

        public void addAbility(GroveAbility ability)
        {
            if(this.groveAbilities.stream().noneMatch(ab -> ab.getName().equalsIgnoreCase(ability.getName())))
                this.groveAbilities.add(ability);
        }

        public boolean isChunkLoading()
        {
            return this.chunkLoading;
        }

        public List<ChunkData> getChunks()
        {
            return this.groveChunks;
        }

        public ChunkData getOrigin()
        {
            return this.origin;
        }

        public @Nullable ChunkData getChunk(int index)
        {
            if (index < 0) return this.origin;

            if (index < this.groveChunks.size())
                return this.groveChunks.get(index);

            return null;
        }

        public boolean addChunk(ChunkData data)
        {
            if (this.groveChunks.stream().noneMatch(gc -> gc.pos().equals(data.pos()))) {
                this.groveChunks.add(data);
                return true;
            }

            return false;
        }

        public void addChunk(ChunkPos pos)
        {
            if (this.groveChunks.stream().noneMatch(gc -> gc.pos().equals(pos)))
                this.groveChunks.add(new ChunkData(pos, false));
        }

        public void removeChunk(ChunkPos pos)
        {
            this.groveChunks.removeIf(gc -> gc.pos().equals(pos));
        }

        public Set<ChunkPos> getAvailableChunks()
        {
            return this.availableChunks;
        }

        public void addAvailable(ChunkPos pos)
        {
            this.availableChunks.add(pos);
        }

        public void removeAvailable(ChunkPos pos)
        {
            this.availableChunks.remove(pos);
        }

        public void setAvailableChunks(Collection<ChunkPos> chunks)
        {
            this.availableChunks.clear();
            this.availableChunks.addAll(chunks);
        }

        public void setMoonwell(BlockPos pos)
        {
            this.moonwell = pos;
        }

        public @Nullable BlockPos getMoonwell()
        {
            return this.moonwell;
        }

        public long getStoredSunlight()
        {
            return this.storedSunlight;
        }

        public long getMaxStoredSunlight()
        {
            return GroveSanctuary.MAX_SUNLIGHT_PER_CHUNK * (this.groveChunks.size() + 1);
        }

        public void setStoredSunlight(long value)
        {
            this.storedSunlight = MathHelper.clamp(value, 0, getMaxStoredSunlight());
        }

        public void addStoredSunlight(long value)
        {
            this.storedSunlight = MathHelper.clamp(this.storedSunlight + value, 0, getMaxStoredSunlight());
        }

        public int getFoliage()
        {
            return this.foliage;
        }

        public void setFoliage(int foliage)
        {
            this.foliage = MathHelper.clamp(foliage, 0, 256 * (this.groveChunks.size() + 1));
        }

        public boolean isEnchanted()
        {
            return this.enchanted;
        }


        public void markDirty()
        {

        }

        public static class ChunkData {
            private final ChunkPos pos;
            private boolean loaded;

            public static final PacketCodec<RegistryByteBuf, ChunkData> PACKET_CODEC = new PacketCodec<RegistryByteBuf, ChunkData>() {
                @Override
                public ChunkData decode(RegistryByteBuf buf) {
                    ChunkPos pos = ChunkPos.PACKET_CODEC.decode(buf);
                    boolean load = buf.readBoolean();

                    return new ChunkData(pos, load);
                }

                @Override
                public void encode(RegistryByteBuf buf, ChunkData value) {
                    ChunkPos.PACKET_CODEC.encode(buf, value.pos());
                    buf.writeBoolean(value.chunkLoad());
                }
            };

            public ChunkData(ChunkPos pos, boolean loaded)
            {
                this.pos = pos;
                this.loaded = loaded;
            }

            public ChunkPos pos() { return this.pos; }
            public boolean chunkLoad() { return this.loaded; }

            public void setLoaded(boolean state)
            {
                this.loaded = state;
            }
        }
    }
}
