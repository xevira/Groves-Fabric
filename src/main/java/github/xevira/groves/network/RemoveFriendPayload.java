package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record RemoveFriendPayload(UUID uuid) implements CustomPayload {
    public static final Id<RemoveFriendPayload> ID = new Id<>(Groves.id("remove_friend"));
    public static final PacketCodec<RegistryByteBuf, RemoveFriendPayload> PACKET_CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, RemoveFriendPayload::uuid,
            RemoveFriendPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
