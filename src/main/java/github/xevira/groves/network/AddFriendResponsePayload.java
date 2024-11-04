package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record AddFriendResponsePayload(UUID uuid, String name, boolean success, Text reason) implements CustomPayload {
    public static final Id<AddFriendResponsePayload> ID = new Id<>(Groves.id("add_friend_response"));
    public static final PacketCodec<RegistryByteBuf, AddFriendResponsePayload> PACKET_CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, AddFriendResponsePayload::uuid,
            PacketCodecs.STRING, AddFriendResponsePayload::name,
            PacketCodecs.BOOL, AddFriendResponsePayload::success,
            TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC, AddFriendResponsePayload::reason,
            AddFriendResponsePayload::new);

    public AddFriendResponsePayload(UUID uuid, String name)
    {
        this(uuid, name, true, Text.empty());
    }

    public AddFriendResponsePayload(String name, Text reason)
    {
        this(new UUID(0L, 0L), name, false, reason);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
