package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record AddFriendPayload(String name) implements CustomPayload {
    public static final Id<AddFriendPayload> ID = new Id<>(Groves.id("add_friend"));
    public static final PacketCodec<RegistryByteBuf, AddFriendPayload> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, AddFriendPayload::name,
            AddFriendPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
