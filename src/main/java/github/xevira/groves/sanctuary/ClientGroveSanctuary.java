package github.xevira.groves.sanctuary;

import com.mojang.authlib.GameProfile;
import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClientGroveSanctuary {
    public static final PacketCodec<RegistryByteBuf, ClientGroveSanctuary> PACKET_CODEC = new PacketCodec<RegistryByteBuf, ClientGroveSanctuary>() {
        @Override
        public ClientGroveSanctuary decode(RegistryByteBuf buf) {
            UUID uuid = Uuids.PACKET_CODEC.decode(buf);
            boolean abandoned = PacketCodecs.BOOL.decode(buf);
            String groveName = PacketCodecs.STRING.decode(buf);
            ChunkData origin = ChunkData.PACKET_CODEC.decode(buf);
            boolean enchanted = PacketCodecs.BOOL.decode(buf);
            boolean chunkLoading = PacketCodecs.BOOL.decode(buf);

            BlockPos spawnPoint = BlockPos.PACKET_CODEC.decode(buf);

            ClientGroveSanctuary sanctuary = new ClientGroveSanctuary(uuid, abandoned, origin, enchanted);

            sanctuary.setGroveName(groveName);
            sanctuary.setSpawnPoint(spawnPoint);
            sanctuary.setChunkLoading(chunkLoading);

            int chunks = PacketCodecs.INTEGER.decode(buf);
            for (int i = 0; i < chunks; i++)
                sanctuary.addChunk(ChunkData.PACKET_CODEC.decode(buf));

            int available = PacketCodecs.INTEGER.decode(buf);
            for (int i = 0; i < available; i++)
                sanctuary.addAvailable(ChunkPos.PACKET_CODEC.decode(buf));

            sanctuary.setFoliage(PacketCodecs.INTEGER.decode(buf));
            sanctuary.setMaxStoredSunlight(PacketCodecs.LONG.decode(buf));
            sanctuary.setStoredSunlight(PacketCodecs.LONG.decode(buf));
            sanctuary.setTotalSunlight(PacketCodecs.LONG.decode(buf));
            sanctuary.setMaxDarkness(PacketCodecs.INTEGER.decode(buf));
            sanctuary.setDarkness(PacketCodecs.INTEGER.decode(buf));
            sanctuary.setTotalDarkness(PacketCodecs.LONG.decode(buf));


            if (PacketCodecs.BOOL.decode(buf))
                sanctuary.setMoonwell(BlockPos.PACKET_CODEC.decode(buf));

            int friends = PacketCodecs.INTEGER.decode(buf);
            for (int i = 0; i < friends; i++) {
                UUID friendID = Uuids.PACKET_CODEC.decode(buf);
                String friendName = PacketCodecs.STRING.decode(buf);
                sanctuary.addFriend(new GameProfile(friendID, friendName));
            }

            int abilities = PacketCodecs.INTEGER.decode(buf);
            for (int i = 0; i < abilities; i++) {
                GroveAbility ability = GroveAbility.PACKET_CODEC.decode(buf);
                if (ability != null)
                    sanctuary.addAbility(ability);
            }

            return sanctuary;
        }

        @Override
        public void encode(RegistryByteBuf buf, ClientGroveSanctuary value) {
            Uuids.PACKET_CODEC.encode(buf, value.uuid);
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
            PacketCodecs.LONG.encode(buf, value.maxSunlight);
            PacketCodecs.LONG.encode(buf, value.storedSunlight);
            PacketCodecs.LONG.encode(buf, value.totalSunlight);
            PacketCodecs.INTEGER.encode(buf, value.maxDarkness);
            PacketCodecs.INTEGER.encode(buf, value.storedDarkness);
            PacketCodecs.LONG.encode(buf, value.totalDarkness);

            BlockPos well = value.getMoonwell();
            if (well != null) {
                PacketCodecs.BOOL.encode(buf, true);
                BlockPos.PACKET_CODEC.encode(buf, well);
            } else
                PacketCodecs.BOOL.encode(buf, false);

            PacketCodecs.INTEGER.encode(buf, value.groveFriends.size());
            value.groveFriends.forEach(friend -> {
                if (friend.getId() != null && friend.getName() != null) {
                    Uuids.PACKET_CODEC.encode(buf, friend.getId());
                    PacketCodecs.STRING.encode(buf, friend.getName());
                }
            });

            PacketCodecs.INTEGER.encode(buf, value.groveAbilities.size());
            value.groveAbilities.forEach(ability -> GroveAbility.PACKET_CODEC.encode(buf, ability));
        }
    };

    private @Nullable GroveSanctuary sanctuary;

    private UUID uuid;
    private boolean abandoned;
    private String groveName;
    private ChunkData origin;
    private BlockPos spawnPoint;
    private boolean enchanted;
    private boolean chunkLoading;

    private final List<ChunkData> groveChunks = new ArrayList<>();
    private int foliage;

    private long storedSunlight;
    private long maxSunlight;
    private long totalSunlight;
    private int storedDarkness;
    private int maxDarkness;
    private long totalDarkness;
    private BlockPos moonwell;

    private final List<GameProfile> groveFriends = new ArrayList<>();
    private final List<GroveAbility> groveAbilities = new ArrayList<>();
    private final Set<ChunkPos> availableChunks = new HashSet<>();

    public ClientGroveSanctuary(UUID uuid, boolean abandoned, ChunkData origin, boolean enchanted) {
        this.uuid = uuid;
        this.abandoned = abandoned;
        this.origin = origin;
        this.enchanted = enchanted;
        this.chunkLoading = false;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public void setAbandoned(boolean abandoned) {
        this.abandoned = abandoned;
    }

    public boolean isAbandoned() {
        return this.abandoned;
    }

    public void setSanctuary(@Nullable GroveSanctuary sanctuary) {
        this.sanctuary = sanctuary;
    }

    public @Nullable GroveSanctuary getSanctuary() {
        return this.sanctuary;
    }

    public String getGroveName() {
        return this.groveName;
    }

    public void setGroveName(@NotNull String name) {
        this.groveName = name;
    }

    public void setSpawnPoint(BlockPos pos) {
        this.spawnPoint = pos;
    }

    public BlockPos getSpawnPoint() {
        return this.spawnPoint;
    }

    public void setChunkLoading(boolean loading) {
        this.chunkLoading = loading;
    }

    public List<GameProfile> getFriends() {
        return this.groveFriends;
    }

    public void addFriend(@NotNull UUID uuid, @NotNull String name) {
        GroveFriend friend = new GroveFriend(uuid, name);
        addFriend(friend);
    }

    public void addFriend(GroveFriend friend) {
        if (this.groveFriends.stream().noneMatch(f -> friend.id().equals(f.getId())))
            this.groveFriends.add(friend.toGameProfile());
    }

    public void addFriend(GameProfile profile) {
        if (profile.getId() == null) return;

        if (this.groveFriends.stream().noneMatch(f -> profile.getId().equals(f.getId())))
            this.groveFriends.add(profile);
    }

    public void removeFriend(@NotNull UUID uuid) {
        Groves.LOGGER.info("CLIENT: Removing Friend: {}", uuid);
        if (this.groveFriends.removeIf(friend -> uuid.equals(friend.getId())))
            Groves.LOGGER.info("CLIENT: Friend removed");
    }

    public void addFriends(List<GroveFriend> friends)
    {
        friends.stream().map(GroveFriend::toGameProfile).forEach(this.groveFriends::add);
    }

    public void removeFriend(int index) {
        if (index >= 0 && index < this.groveFriends.size())
            this.groveFriends.remove(index);
    }

    public List<GroveAbility> getAbilities() {
        return this.groveAbilities;
    }

    public void addAbility(GroveAbility ability) {
        if (this.groveAbilities.stream().noneMatch(ab -> ab.getName().equalsIgnoreCase(ability.getName())))
            this.groveAbilities.add(ability);
    }

    public void updateAbility(String name, boolean active, long start, long end, int rank) {
        this.groveAbilities.stream().filter(ability -> ability.getName().equalsIgnoreCase(name)).findFirst().ifPresent(ability -> {
            ability.setActive(active);
            ability.setCooldown(start, end);
            ability.setRank(rank);
        });
    }

    public void addAbilities(List<GroveAbility> abilities)
    {
        this.groveAbilities.addAll(abilities);
    }

    public boolean isChunkLoading() {
        return this.chunkLoading;
    }

    public List<ChunkData> getChunks() {
        return this.groveChunks;
    }

    public ChunkData getOrigin() {
        return this.origin;
    }

    public @Nullable ChunkData getChunk(int index) {
        if (index < 0) return this.origin;

        if (index < this.groveChunks.size())
            return this.groveChunks.get(index);

        return null;
    }

    public boolean addChunk(ChunkData data) {
        if (this.groveChunks.stream().noneMatch(gc -> gc.pos().equals(data.pos()))) {
            this.groveChunks.add(data);
            return true;
        }

        return false;
    }

    public void addChunk(ChunkPos pos) {
        if (this.groveChunks.stream().noneMatch(gc -> gc.pos().equals(pos)))
            this.groveChunks.add(new ChunkData(pos, false));
    }

    public void removeChunk(ChunkPos pos) {
        this.groveChunks.removeIf(gc -> gc.pos().equals(pos));
    }

    public int totalChunks() {
        return this.groveChunks.size() + 1;
    }

    public Set<ChunkPos> getAvailableChunks() {
        return this.availableChunks;
    }

    public void addAvailable(ChunkPos pos) {
        this.availableChunks.add(pos);
    }

    public void removeAvailable(ChunkPos pos) {
        this.availableChunks.remove(pos);
    }

    public void setAvailableChunks(Collection<ChunkPos> chunks) {
        this.availableChunks.clear();
        this.availableChunks.addAll(chunks);
    }

    public void setMoonwell(BlockPos pos) {
        this.moonwell = pos;
    }

    public @Nullable BlockPos getMoonwell() {
        return this.moonwell;
    }

    public long getStoredSunlight() {
        return this.storedSunlight;
    }

    public void setMaxStoredSunlight(long value) {
        this.maxSunlight = value;
    }

    public long getMaxStoredSunlight() {
        return this.maxSunlight * totalChunks();
    }

    public void setStoredSunlight(long value) {
        this.storedSunlight = MathHelper.clamp(value, 0, getMaxStoredSunlight());
    }

    public void addStoredSunlight(long value) {
        this.storedSunlight = MathHelper.clamp(this.storedSunlight + value, 0, getMaxStoredSunlight());
    }

    public void setTotalSunlight(long value) {
        this.totalSunlight = value;
    }

    public long getTotalSunlight() {
        return this.totalSunlight;
    }

    public void setMaxDarkness(int value) {
        this.maxDarkness = value;
    }

    public int getMaxDarkness() {
        return this.maxDarkness;
    }

    public void setDarkness(int value) {
        this.storedDarkness = MathHelper.clamp(value, 0, getMaxDarkness());
    }

    public int getDarkness() {
        return this.storedDarkness;
    }

    public void setTotalDarkness(long value) {
        this.totalDarkness = value;
    }

    public long getTotalDarkness() {
        return this.totalDarkness;
    }

    public int getFoliage() {
        return this.foliage;
    }

    public void setFoliage(int foliage) {
        this.foliage = Math.max(foliage, 0);
    }

    public boolean isEnchanted() {
        return this.enchanted;
    }


    public void markDirty() {

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

        public ChunkData(ChunkPos pos, boolean loaded) {
            this.pos = pos;
            this.loaded = loaded;
        }

        public ChunkPos pos() {
            return this.pos;
        }

        public boolean chunkLoad() {
            return this.loaded;
        }

        public void setLoaded(boolean state) {
            this.loaded = state;
        }
    }
}
