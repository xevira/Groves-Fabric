package github.xevira.groves.sanctuary;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.authlib.GameProfile;
import github.xevira.groves.util.JSONHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Uuids;

import java.util.Optional;
import java.util.UUID;

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


    public GroveFriend(GameProfile profile) {
        this(profile.getId(), profile.getName());
    }

    public GroveFriend(PlayerEntity friend) {
        this(friend.getGameProfile());
    }

    public boolean isFriend(PlayerEntity friend) {
        return id.equals(friend.getUuid());
    }

    public GameProfile toGameProfile() {
        return new GameProfile(this.id, this.name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GroveFriend friend) {
            if (!friend.id.equals(this.id)) return false;
            if (!friend.name.equals(this.name)) return false;

            return true;
        }

        if (obj instanceof UUID uuid) {
            return this.id.equals(uuid);
        }

        return false;
    }

    public JsonObject serialize() {
        JsonObject json = new JsonObject();

        json.add("name", new JsonPrimitive(this.name));
        json.add("id", new JsonPrimitive(this.id.toString()));

        return json;
    }

    public static Optional<GroveFriend> deserialize(JsonObject json) {
        Optional<UUID> id = JSONHelper.getUUID(json, "id");
        String name = JSONHelper.getString(json, "name");

        if (id.isPresent() && name != null)
            return Optional.of(new GroveFriend(id.get(), name));

        return Optional.empty();
    }
}
