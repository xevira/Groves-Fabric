package github.xevira.groves.sanctuary;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.authlib.GameProfile;
import github.xevira.groves.Groves;
import github.xevira.groves.ServerConfig;
import github.xevira.groves.network.*;
import github.xevira.groves.poi.GrovesPOI;
import github.xevira.groves.screenhandler.GrovesSanctuaryScreenHandler;
import github.xevira.groves.util.ChunkHelper;
import github.xevira.groves.util.JSONHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.UserCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GroveSanctuary  implements ExtendedScreenHandlerFactory<GrovesSanctuaryScreenPayload> {
    public static final Text TITLE = Groves.text("gui", "groves");

    public static final Text INVALID_CHUNK_ERROR = Groves.text("error", "groves.claim_chunk.invalid");
    public static final Text ALREADY_OWN_ERROR = Groves.text("error", "groves.claim_chunk.own");
    public static final Text CLAIMED_ERROR = Groves.text("error", "groves.claim_chunk.claimed");

    private final MinecraftServer server;
    private final UUID uuid;
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
    private long totalSunlight;
    private long totalDarkness;
    private long storedSunlight;
    private int storedDarkness;
    private @Nullable BlockPos moonwell;
    private boolean chunkLoaded;
    private BlockPos spawnPoint;

    // Transient (will never save)
    private int lastServerTick;
    private int updatingTickChunk;
    private int lastTotalFoliage = -1;

    private int skyColor = -1;
    private int grassColor = -1;
    private int foliageColor = -1;
    private int waterColor = -1;

    private int lastSkyColor = -1;
    private int lastGrassColor = -1;
    private int lastFoliageColor = -1;
    private int lastWaterColor = -1;

    private final Set<ServerPlayerEntity> listeners = new HashSet<>();

    private final List<GroveUnlock> unlocks = new ArrayList<>();

    public GroveSanctuary(final MinecraftServer server, final PlayerEntity player, final ServerWorld world, final ChunkPos pos, final boolean enchanted)
    {
        this(server, UUID.randomUUID(), player.getUuid(), player.getName().getString(), world, pos, enchanted);
    }

    public GroveSanctuary(final MinecraftServer server, final UUID uuid, final UUID owner, final String ownerName, final ServerWorld world, final ChunkPos pos, final boolean enchanted)
    {
        this(server, uuid, false, owner, ownerName, world, new GroveChunkData(world, pos), enchanted);
    }

    public GroveSanctuary(final MinecraftServer server, final UUID uuid, final ServerWorld world, final ChunkPos pos, final boolean enchanted)
    {
        this(server, uuid, true, null, "", world, new GroveChunkData(world, pos), enchanted);
    }

    public GroveSanctuary(final MinecraftServer server, final UUID uuid, final boolean abandoned, final UUID owner, final String ownerName, final ServerWorld world, final GroveChunkData data, final boolean enchanted)
    {
        this.server = server;
        this.uuid = uuid;
        this.owner = owner;
        this.ownerName = ownerName;
        this.abandoned = abandoned;
        this.groveName = "";
        this.world = world;
        this.origin = data;
        this.origin.setSanctuary(this);
        this.enchanted = enchanted;

        this.storedSunlight = 0;
        this.storedDarkness = 0;
        this.totalSunlight = 0;
        this.totalDarkness = 0;
        this.moonwell = null;
        this.chunkLoaded = false;

        this.lastServerTick = 0;
        this.updatingTickChunk = -1; // -1 == Origin
    }

    public UUID getUUID()
    {
        return this.uuid;
    }

    public ServerWorld getWorld()
    {
        return this.world;
    }

    public void abandonSanctuary()
    {
        this.abandoned = true;
        this.owner = null;
        this.ownerName = "";
        this.unlocks.clear();
        this.groveFriends.clear();
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
        this.storedSunlight = 0L;   // All sunlight is lost

        // Reset the abilities to the original default list
        // All previous abilities are lost, as the new owner did not earn them.
        this.groveAbilities.clear();
        GroveAbilities.autoInstallAbilities(this);

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

    public void sendOwner(CustomPayload payload, boolean inside)
    {
        ServerPlayerEntity owner = getOwnerPlayer();

        if (owner != null && (!inside || isPlayerInSanctuary(owner)))
        {
            ServerPlayNetworking.send(owner, payload);
        }
    }


    public boolean hasUnlock(GroveUnlock unlock)
    {
        return this.unlocks.stream().anyMatch(u -> u.getName().equalsIgnoreCase(unlock.getName()));
    }

    public boolean hasUnlock(String name)
    {
        return this.unlocks.stream().anyMatch(u -> u.getName().equalsIgnoreCase(name));
    }

    public void grantUnlock(GroveUnlock unlock)
    {
        if (!hasUnlock(unlock))
            this.unlocks.add(unlock);
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
            Optional<github.xevira.groves.sanctuary.GroveSanctuary> sanc = GrovesPOI.getSanctuary(this.world, adj);

            if (sanc.isEmpty() && GrovesPOI.isChunkValidForGrove(this.world, adj, this.enchanted))
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

    public boolean isPlayerInSanctuary(PlayerEntity player)
    {
        if (player == null) return false;

        return contains(player.getBlockPos());
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

    public int getSunlightPercent() {
        return (int)(100 * this.storedSunlight / this.getMaxStoredSunlight());
    }
    public int getDarknessPercent() { return (100 * this.storedDarkness / ServerConfig.maxDarkness()); }

    public long getTotalSunlight()
    {
        return this.totalSunlight;
    }

    public long getTotalDarkness()
    {
        return this.totalDarkness;
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
        return ServerConfig.sunlightPerChunk() * totalChunks();
    }

    /**
     *  <p>Accumulates sunlight for the Grove Sanctuary.  Supplying a negative value will drain the reservoir.</p>
     **/
    public void addSunlight(long sunlight)
    {
        sunlight = Math.max(0L, sunlight);

        this.storedSunlight = MathHelper.clamp(this.storedSunlight + sunlight, 0, getMaxStoredSunlight());
        this.totalSunlight += sunlight;
        sendListeners(new UpdateSunlightPayload(this.storedSunlight, this.totalSunlight));
    }

    public void useSunlight(long sunlight)
    {
        sunlight = Math.max(0L, sunlight);

        this.storedSunlight = MathHelper.clamp(this.storedSunlight - sunlight, 0, getMaxStoredSunlight());
        sendListeners(new UpdateSunlightPayload(this.storedSunlight, this.totalSunlight));
    }

    public void addDarkness(int darkness)
    {
        darkness = Math.max(0, darkness);

        this.storedDarkness = MathHelper.clamp(this.storedDarkness + darkness, 0, ServerConfig.maxDarkness());
        this.totalDarkness += darkness;
        sendListeners(new UpdateDarknessPayload(this.storedDarkness, this.totalDarkness));
    }

    public void useDarkness(int darkness)
    {
        darkness = Math.max(0, darkness);

        this.storedDarkness = MathHelper.clamp(this.storedDarkness - darkness, 0, ServerConfig.maxDarkness());
        sendListeners(new UpdateDarknessPayload(this.storedDarkness, this.totalDarkness));
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
        return this.groveChunks.size() + 1;     // +1 for the origin
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

            GrovesPOI.registerGroveChunk(this.world, pos, this);
            return true;
        }

        return false;
    }

    public void claimChunk(ServerPlayerEntity player, ChunkPos pos)
    {
        Optional<github.xevira.groves.sanctuary.GroveSanctuary> sanc = GrovesPOI.getSanctuary(this.world, pos);

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

            GrovesPOI.unregisterGroveChunk(this.world, pos);
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

    public static final Text ALREADY_FRIEND_TEXT = Groves.text("error", "already.friend");
    public static final Text PLAYER_NOT_FOUND_TEXT = Groves.text("error", "player.not.found");
    public static final Text NOT_FRIEND_TEXT = Groves.text("error", "player.not.friend");
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
                    ServerPlayNetworking.send(player, new AddFriendResponsePayload(name, ALREADY_FRIEND_TEXT));
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
                ServerPlayNetworking.send(player, new AddFriendResponsePayload(name, PLAYER_NOT_FOUND_TEXT));
            }

        }
    }

    public void removeFriend(ServerPlayerEntity player, UUID uuid)
    {
        if (this.groveFriends.removeIf(friend -> friend.id().equals(uuid)))
        {
            sendListeners(new RemoveFriendResponsePayload(uuid));
        }
        else
        {
            ServerPlayNetworking.send(player, new RemoveFriendResponsePayload(NOT_FRIEND_TEXT));
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

    public void installAbility(GroveAbility prototype, int rank)
    {
        if (!hasAbility(prototype.getName())) {
            GroveAbility ability = prototype.getConstructor().get();
            ability.setRank(rank);
            this.groveAbilities.add(ability);

            // TODO: sendListeners
        }
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

        float powerRating = ServerConfig.getFoliagePowerRating(this.world.isRaining());

        if (this.enchanted) powerRating *= ServerConfig.getFoliageEnchantedMultiplier();

        // World must have daylight
        if (this.world.isDay()) {
            // Step 3: convert foliage count into solar power
            long solarPower = (long) (totalFoliage * powerRating);

            if (solarPower > 0)
                addSunlight(solarPower);
        }

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
                    ability.deactivate(this.server, this, null);
                }
                else if (ability.onServerTick(this.server, this)) {
                    if (ability.autoDeactivate()) {
                        ability.setActive(false);
                        ability.deactivate(this.server, this, null);
                    }
                }
            }
        });
    }

    private void processCooldowns()
    {
        this.groveAbilities.forEach(ability -> {
            if (ability.getEndCooldown() > 0 && this.world.getTimeOfDay() >= ability.getEndCooldown())
            {
                ability.clearCooldown();
                // Update ability to listeners?
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
        updateColorData();

        // Only check unlocks if the owner is online
        if (!isAbandoned() && isOwnerOnline(this.server))
            GroveUnlocks.checkUnlocks(this.server, this, getOwnerPlayer());

        processFoliage();
        processAbilities();
        processCooldowns();
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

        json.add("uuid", new JsonPrimitive(this.uuid.toString()));

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

        if (this.skyColor >= 0) json.add("skyColor", JSONHelper.ColorToJson(this.skyColor));
        if (this.grassColor >= 0) json.add("grassColor", JSONHelper.ColorToJson(this.grassColor));
        if (this.foliageColor >= 0) json.add("foliageColor", JSONHelper.ColorToJson(this.foliageColor));
        if (this.waterColor >= 0) json.add("waterColor", JSONHelper.ColorToJson(this.waterColor));

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

        JsonArray unlocks = new JsonArray();
        this.unlocks.forEach(unlock -> unlocks.add(new JsonPrimitive(unlock.getName())));
        json.add("unlocks", unlocks);

        json.add("sunlight", new JsonPrimitive(this.storedSunlight));
        json.add("darkness", new JsonPrimitive(this.storedDarkness));

        json.add("totalSunlight", new JsonPrimitive(this.totalSunlight));
        json.add("totalDarkness", new JsonPrimitive(this.totalDarkness));

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

    private static List<GroveUnlock> deserializeUnlocks(JsonObject json)
    {
        List<GroveUnlock> unlocks = new ArrayList<>();

        if (json.has("unlocks"))
        {
            JsonElement element = json.get("unlocks");
            if (element.isJsonArray())
            {
                JsonArray array = element.getAsJsonArray();

                for(JsonElement item : array)
                {
                    if (item.isJsonPrimitive())
                    {
                        JsonPrimitive p = item.getAsJsonPrimitive();

                        if (p.isString())
                        {
                            GroveUnlock unlock = GroveUnlocks.getByName(p.getAsString());

                            if (unlock != null)
                                unlocks.add(unlock);
                        }
                    }
                }
            }
        }

        return unlocks;
    }


    public static Optional<github.xevira.groves.sanctuary.GroveSanctuary> deserialize(JsonObject json, MinecraftServer server)
    {
        boolean abandoned = JSONHelper.getBoolean(json, "abandoned", false);

        UUID uuid = JSONHelper.getUUID(json, "uuid").orElse(UUID.randomUUID());

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
        Optional<Integer> storedDarkness = JSONHelper.getInt(json, "darkness");
        Optional<Long> totalSunlight = JSONHelper.getLong(json, "totalSunlight");
        Optional<Long> totalDarkness = JSONHelper.getLong(json, "totalDarkness");


        List<GroveChunkData> chunks = deserializeChunks(json, server, world);
        Set<ChunkPos> available = deserializeAvailable(json);
        List<GroveFriend> friends = deserializeFriends(json);
        List<GroveAbility> abilities = deserializeAbilities(json);
        List<GroveUnlock> unlocks = deserializeUnlocks(json);

        github.xevira.groves.sanctuary.GroveSanctuary sanctuary = new github.xevira.groves.sanctuary.GroveSanctuary(server, uuid, abandoned, owner, ownerName, world, origin.get(), enchanted.orElse(false));

        sanctuary.groveName = groveName;

        spawnPoint.ifPresent(sanctuary::setSpawnPoint);
        sanctuary.groveChunks.addAll(chunks);
        sanctuary.groveFriends.addAll(friends);
        sanctuary.groveAbilities.addAll(abilities);
        GroveAbilities.autoInstallAbilities(sanctuary); // Add any missing abilities the sanctuary should have at the start
        sanctuary.availableChunks.addAll(available);
        sanctuary.unlocks.addAll(unlocks);

        JSONHelper.JsonToColor(json, "skyColor").ifPresent(color -> sanctuary.skyColor = color);
        JSONHelper.JsonToColor(json, "grassColor").ifPresent(color -> sanctuary.grassColor = color);
        JSONHelper.JsonToColor(json, "foliageColor").ifPresent(color -> sanctuary.foliageColor = color);
        JSONHelper.JsonToColor(json, "waterColor").ifPresent(color -> sanctuary.waterColor = color);

        chunkLoaded.ifPresent(aBoolean -> sanctuary.chunkLoaded = aBoolean);
        storedSunlight.ifPresent(sunlight -> sanctuary.storedSunlight = sunlight);
        storedDarkness.ifPresent(darkness -> sanctuary.storedDarkness = darkness);
        totalSunlight.ifPresent(sunlight -> sanctuary.totalSunlight = sunlight);
        totalDarkness.ifPresent(darkness -> sanctuary.totalDarkness = darkness);
        moonwell.ifPresent(blockPos -> sanctuary.moonwell = blockPos);

        GrovesPOI.registerGroveChunk(world, origin.get().chunkPos, sanctuary);
        chunks.forEach(chunk -> GrovesPOI.registerGroveChunk(world, chunk.chunkPos, sanctuary));

        return Optional.of(sanctuary);
    }

    public ClientGroveSanctuary createClientData()
    {
        ClientGroveSanctuary sanctuary = new ClientGroveSanctuary(this.uuid, this.abandoned, new ClientGroveSanctuary.ChunkData(this.origin.chunkPos, this.origin.chunkLoad), this.enchanted);
        sanctuary.setGroveName(this.groveName);
        sanctuary.setSpawnPoint(this.spawnPoint);
        sanctuary.setChunkLoading(this.chunkLoaded);

        this.groveChunks.stream().map(GroveChunkData::toClientData).forEach(sanctuary::addChunk);
        sanctuary.addFriends(this.groveFriends);

        sanctuary.setFoliage(groveChunks.stream().map(chunk -> chunk.foliage).reduce(this.origin.foliage, Integer::sum));
        sanctuary.setMaxStoredSunlight(ServerConfig.sunlightPerChunk());
        sanctuary.setStoredSunlight(this.storedSunlight);
        sanctuary.setTotalSunlight(this.totalSunlight);
        sanctuary.setMaxDarkness(ServerConfig.maxDarkness());
        sanctuary.setDarkness(this.storedDarkness);
        sanctuary.setTotalDarkness(this.totalDarkness);
        sanctuary.setMoonwell(this.moonwell);

        sanctuary.addAbilities(this.groveAbilities);
        sanctuary.setAvailableChunks(this.availableChunks);

        return sanctuary;
    }

    private GrovesPOI.ClientGroveSanctuaryColorData createColorData()
    {
        return new GrovesPOI.ClientGroveSanctuaryColorData(skyColor, grassColor, foliageColor, waterColor);
    }

    // Need to send this to players when they connect...
    public void syncColorData(ServerPlayerEntity player)
    {
        GrovesPOI.ClientGroveSanctuaryColorData newColors = createColorData();

        List<ChunkPos> chunks = new ArrayList<>();
        chunks.add(this.origin.chunkPos);
        this.groveChunks.forEach(chunk -> chunks.add(chunk.chunkPos));

        SyncChunkColorsPayload payload = new SyncChunkColorsPayload(this.world.getRegistryKey().getValue().toString(), chunks, newColors);

        ServerPlayNetworking.send(player, payload);
    }

    public void updateColorData()
    {
        if (skyColor != lastSkyColor ||
            grassColor != lastGrassColor ||
            foliageColor != lastFoliageColor ||
            waterColor != lastWaterColor)
        {
            lastSkyColor = skyColor;
            lastGrassColor = grassColor;
            lastFoliageColor = foliageColor;
            lastWaterColor = waterColor;

            // Don't waste your time if no one is even online
            if (this.server.getPlayerManager().getPlayerList().isEmpty()) return;

            GrovesPOI.ClientGroveSanctuaryColorData newColors = createColorData();

            List<ChunkPos> chunks = new ArrayList<>();
            chunks.add(this.origin.chunkPos);
            this.groveChunks.forEach(chunk -> chunks.add(chunk.chunkPos));

            SyncChunkColorsPayload payload = new SyncChunkColorsPayload(this.world.getRegistryKey().getValue().toString(), chunks, newColors);

            // Send to all players
            for(ServerPlayerEntity player : this.server.getPlayerManager().getPlayerList())
                ServerPlayNetworking.send(player, payload);
        }
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
        private github.xevira.groves.sanctuary.GroveSanctuary sanctuary;
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

        public GroveChunkData(github.xevira.groves.sanctuary.GroveSanctuary sanctuary, ServerWorld world, ChunkPos chunk)
        {
            this(world, chunk);
            this.sanctuary = sanctuary;
        }

        public ClientGroveSanctuary.ChunkData toClientData()
        {
            return new ClientGroveSanctuary.ChunkData(this.chunkPos, this.chunkLoad);
        }

        public void setSanctuary(github.xevira.groves.sanctuary.GroveSanctuary sanctuary)
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
            return !state.contains(net.minecraft.state.property.Properties.PERSISTENT) || !state.get(Properties.PERSISTENT);
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

                    if (isValidLeaf(state)) {
                        leaves += ServerConfig.getFoliagePower(state.getBlock());
                    }
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
