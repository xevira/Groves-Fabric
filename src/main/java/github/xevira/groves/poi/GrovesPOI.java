package github.xevira.groves.poi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.authlib.GameProfile;
import github.xevira.groves.Groves;
import github.xevira.groves.network.GrovesSanctuaryScreenPayload;
import github.xevira.groves.network.UpdateSunlightPayload;
import github.xevira.groves.network.UpdateTotalFoliagePayload;
import github.xevira.groves.screenhandler.GrovesSanctuaryScreenHandler;
import github.xevira.groves.util.JSONHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/** Manages the POIs for player-made Grove Sanctuaries **/
public class GrovesPOI {
    private static String JSON_GROUP = "sanctuaries";

    private static final List<GroveSanctuary> SANCTUARIES = new ArrayList<>();

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

    public static boolean sanctuaryExists(ServerWorld world, ChunkPos pos)
    {
        return SANCTUARIES.stream().anyMatch(groveSanctuary -> groveSanctuary.contains(world, pos));
    }

    public static ImprintSanctuaryResult imprintSanctuary(PlayerEntity player, ServerWorld world, BlockPos pos, boolean enchanted)
    {
        ChunkPos chunkPos = new ChunkPos(pos);

        if (hasSanctuary(player))
            return ImprintSanctuaryResult.ALREADY_HAS_GROVE;

        if (sanctuaryExists(world, chunkPos))
            return ImprintSanctuaryResult.ALREADY_EXISTS;

        // Create the Grove Sanctuary
        GroveSanctuary sanctuary = new GroveSanctuary(player, world, chunkPos, enchanted);
        SANCTUARIES.add(sanctuary);

        return ImprintSanctuaryResult.SUCCESS;
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

        json.add(JSON_GROUP, sanctuaries);
    }

    public static boolean deserializeServer(JsonObject json, MinecraftServer server)
    {
        SANCTUARIES.clear();

        if (json.has(JSON_GROUP))
        {
            JsonElement element = json.get(JSON_GROUP);

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

    public enum ImprintSanctuaryResult {
        /** Player was able to imprint a Grove Sanctuary **/
        SUCCESS,

        /** Chunk is already part of a Grove Sanctuary **/
        ALREADY_EXISTS,

        /** Player already possesses a Grove Sanctuary **/
        ALREADY_HAS_GROVE;
    }

    public static class GroveSanctuary implements ExtendedScreenHandlerFactory<GrovesSanctuaryScreenPayload> {
        public static final Text TITLE = Groves.text("gui", "groves");

        public static final long MAX_SUNLIGHT_PER_CHUNK = 1000000L;

        private final UUID owner;
        private final String ownerName;

        private final ServerWorld world;
        private final GroveChunkData origin;
        private final List<GroveChunkData> groveChunks = new ArrayList<>();
        private final List<GroveFriend> groveFriends = new ArrayList<>();
        private final boolean enchanted;

        // Variable properties
        private long storedSunlight;
        private @Nullable BlockPos moonwell;

        // Transient (will never save)
        private int lastServerTick;
        private int updatingTickChunk;
        private int lastTotalFoliage = -1;

        public GroveSanctuary(final PlayerEntity player, final ServerWorld world, final ChunkPos pos, final boolean enchanted)
        {
            this(player.getUuid(), player.getName().getString(), world, pos, enchanted);
        }

        public GroveSanctuary(final UUID owner, final String ownerName, final ServerWorld world, final ChunkPos pos, final boolean enchanted)
        {
            this(owner, ownerName, world, new GroveChunkData(world, pos), enchanted);
        }

        public GroveSanctuary(final UUID owner, final String ownerName, final ServerWorld world, final GroveChunkData data, final boolean enchanted)
        {
            this.owner = owner;
            this.ownerName = ownerName;
            this.world = world;
            this.origin = data;
            this.enchanted = enchanted;

            this.storedSunlight = 0;
            this.moonwell = null;

            this.lastServerTick = 0;
            this.updatingTickChunk = -1; // -1 == Origin
        }

        public UUID getOwner()
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
        }

        public void clearMoonwell()
        {
            this.moonwell = null;
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
        }

        public boolean sameWorld(@NotNull ServerWorld world)
        {
            return this.world == world;
        }

        /**
         * Adds the specified chunk to the Grove Sanctuary.
         *
         **/
        public boolean addChunk(ChunkPos pos)
        {
            if(groveChunks.stream().noneMatch(chunk -> chunk.isChunk(pos)))
                return groveChunks.add(new GroveChunkData(this.world, pos));

            return false;
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

                return true;
            }

            return false;
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

        public boolean contains(BlockPos pos)
        {
            ChunkPos chunkPos = new ChunkPos(pos);
            return contains(chunkPos);
        }

        /** Indicates whether the Grove Sanctuary is in the specified world and contains the specified chunk **/
        public boolean contains(ServerWorld world, ChunkPos pos)
        {
            if (!sameWorld(world)) return false;

            return contains(pos);
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

        private ServerPlayerEntity getOwnerPlayer(MinecraftServer server)
        {
            return server.getPlayerManager().getPlayer(this.owner);
        }

        private void sendOwner(MinecraftServer server, CustomPayload payload)
        {
            ServerPlayerEntity player = getOwnerPlayer(server);

            if (player != null)
            {
                ServerPlayNetworking.send(player, payload);
            }
        }

        /** Server Tick event used to generate passive sunlight **/
        public void onEndServerTick(MinecraftServer server)
        {
            if (this.lastServerTick-- > 0)
                return;

            this.lastServerTick = 20;   // Once a second

            // Check if the owner is online
            if (!isOwnerOnline(server)) return;

            // World must have daylight
            if (!this.world.isDay()) return;

            // Step 1: update foliage count in currently selected chunk
            GroveChunkData data = getUpdatingChunk();
            if (data.onServerTick(server))
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

            if (solarPower > 0) {
                addSunlight(solarPower);

                sendOwner(server, new UpdateSunlightPayload(this.storedSunlight));
            }

            if (totalFoliage != lastTotalFoliage)
            {
                lastTotalFoliage = totalFoliage;
                sendOwner(server, new UpdateTotalFoliagePayload(totalFoliage));
            }
        }

        public JsonObject serialize(MinecraftServer server)
        {
            JsonObject json = new JsonObject();

            json.add("owner", new JsonPrimitive(this.owner.toString()));
            json.add("ownerName", new JsonPrimitive(this.ownerName));

            json.add("world", new JsonPrimitive(this.world.getRegistryKey().getValue().toString()));
            json.add("origin", this.origin.serializeServer(server));

            json.add("enchanted", new JsonPrimitive(this.enchanted));

            if (this.moonwell != null)
                json.add("moonwell", JSONHelper.BlockPosToJson(this.moonwell));

            JsonArray chunks = new JsonArray();
            this.groveChunks.stream().map(chunk -> chunk.serializeServer(server)).forEach(chunks::add);
            json.add("chunks", chunks);

            JsonArray friends = new JsonArray();
            this.groveFriends.stream().map(GroveFriend::serialize).forEach(friends::add);
            json.add("friends", friends);

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

        public static Optional<GroveSanctuary> deserialize(JsonObject json, MinecraftServer server)
        {
            Optional<UUID> owner = JSONHelper.getUUID(json, "owner");
            if (owner.isEmpty()) {
                Groves.LOGGER.error("Invalid owner in Groves Sanctuary");
                return Optional.empty();
            }

            String ownerName = JSONHelper.getString(json, "ownerName");
            if (ownerName == null) {
                Groves.LOGGER.error("Invalid owner name in Groves Sanctuary");
                return Optional.empty();
            }

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

            Optional<Boolean> enchanted = JSONHelper.getBoolean(json, "enchanted");
            Optional<BlockPos> moonwell = JSONHelper.JsonToBlockPos(json, "moonwell");

            // If it is empty, it will reset to 0.
            Optional<Long> storedSunlight = JSONHelper.getLong(json, "sunlight");

            List<GroveChunkData> chunks = deserializeChunks(json, server, world);
            List<GroveFriend> friends = deserializeFriends(json);

            GroveSanctuary sanctuary = new GroveSanctuary(owner.get(), ownerName, world, origin.get(), enchanted.isPresent() && enchanted.get());

            sanctuary.groveChunks.addAll(chunks);
            sanctuary.groveFriends.addAll(friends);

            storedSunlight.ifPresent(sunlight -> sanctuary.storedSunlight = sunlight);
            moonwell.ifPresent(blockPos -> sanctuary.moonwell = blockPos);

            return Optional.of(sanctuary);
        }

        public ClientGroveSanctuary createClientData()
        {
            ClientGroveSanctuary sanctuary = new ClientGroveSanctuary(this.origin.chunkPos, this.enchanted);

            this.groveChunks.stream().map(groveChunk -> groveChunk.chunkPos).forEach(sanctuary::addChunk);
            sanctuary.groveFriends.addAll(this.groveFriends);

            sanctuary.setFoliage(groveChunks.stream().map(chunk -> chunk.foliage).reduce(this.origin.foliage, Integer::sum));
            sanctuary.setStoredSunlight(this.storedSunlight);
            sanctuary.setMoonwell(this.moonwell);

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
            public final ServerWorld world;
            public final ChunkPos chunkPos;

            public int foliage;
            public final int[] foliageRows = new int[16];

            private int updatingRow;

            public GroveChunkData(ServerWorld world, ChunkPos chunk)
            {
                this.world = world;
                this.chunkPos = chunk;
                this.foliage = 0;
                Arrays.fill(this.foliageRows, 0);

                this.updatingRow = 0;
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

                return json;
            }

            public static Optional<GroveChunkData> deserializeServer(JsonObject json, MinecraftServer server, ServerWorld world)
            {
                Optional<ChunkPos> chunk = JSONHelper.JsonToChunkPos(json, "chunk");
                Optional<Integer> foliage = JSONHelper.getInt(json, "foliage");
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
                ChunkPos origin = ChunkPos.PACKET_CODEC.decode(buf);
                boolean enchanted = PacketCodecs.BOOL.decode(buf);

                ClientGroveSanctuary sanctuary = new ClientGroveSanctuary(origin, enchanted);

                int chunks = PacketCodecs.INTEGER.decode(buf);
                for(int i = 0; i < chunks; i++)
                    sanctuary.addChunk(ChunkPos.PACKET_CODEC.decode(buf));

                sanctuary.setFoliage(PacketCodecs.INTEGER.decode(buf));
                sanctuary.setStoredSunlight(PacketCodecs.LONG.decode(buf));

                if (PacketCodecs.BOOL.decode(buf))
                    sanctuary.setMoonwell(BlockPos.PACKET_CODEC.decode(buf));

                int friends = PacketCodecs.INTEGER.decode(buf);
                for(int i = 0; i < friends; i++)
                    sanctuary.addFriend(GroveFriend.PACKET_CODEC.decode(buf));

                return sanctuary;
            }

            @Override
            public void encode(RegistryByteBuf buf, ClientGroveSanctuary value) {
                ChunkPos.PACKET_CODEC.encode(buf, value.origin);
                PacketCodecs.BOOL.encode(buf, value.enchanted);

                PacketCodecs.INTEGER.encode(buf, value.groveChunks.size());
                value.groveChunks.forEach(chunk -> ChunkPos.PACKET_CODEC.encode(buf, chunk));

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
                value.groveFriends.forEach(friend -> GroveFriend.PACKET_CODEC.encode(buf, friend));

            }
        };

        private ChunkPos origin;
        private boolean enchanted;

        private final List<ChunkPos> groveChunks = new ArrayList<>();
        private int foliage;

        private long storedSunlight;
        private BlockPos moonwell;

        private final List<GroveFriend> groveFriends = new ArrayList<>();

        public ClientGroveSanctuary(ChunkPos origin, boolean enchanted)
        {
            this.origin = origin;
            this.enchanted = enchanted;
        }

        public void addFriend(@NotNull UUID uuid, @NotNull String name)
        {
            GroveFriend friend = new GroveFriend(uuid, name);
            addFriend(friend);
        }

        public void addFriend(GroveFriend friend)
        {
            if (!this.groveFriends.contains(friend))
                this.groveFriends.add(friend);
        }

        public void removeFriend(@NotNull UUID uuid)
        {
            this.groveFriends.removeIf(friend -> friend.id.equals(uuid));
        }

        public void removeFriend(int index)
        {
            if (index >= 0 && index < this.groveFriends.size())
                this.groveFriends.remove(index);
        }

        public @Nullable ChunkPos getChunk(int index)
        {
            if (index < 0) return this.origin;

            if (index < this.groveChunks.size())
                return this.groveChunks.get(index);

            return null;
        }

        public void addChunk(ChunkPos pos)
        {
            if (!this.groveChunks.contains(pos)) {
                this.groveChunks.add(pos);
            }
        }

        public void removeChunk(ChunkPos pos)
        {
            this.groveChunks.remove(pos);
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
    }
}
