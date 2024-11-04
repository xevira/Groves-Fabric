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

public record RemoveFriendResponsePayload(UUID uuid, boolean success, Text reason) implements CustomPayload {
    public static final Id<RemoveFriendResponsePayload> ID = new Id<>(Groves.id("remove_friend"));
    public static final PacketCodec<RegistryByteBuf, RemoveFriendResponsePayload> PACKET_CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, RemoveFriendResponsePayload::uuid,
            PacketCodecs.BOOL, RemoveFriendResponsePayload::success,
            TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC, RemoveFriendResponsePayload::reason,
            RemoveFriendResponsePayload::new);

    public RemoveFriendResponsePayload(UUID uuid)
    {
        this(uuid, true, Text.empty());
    }

    public RemoveFriendResponsePayload(Text reason)
    {
        this(new UUID(0L, 0L), false, reason);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
