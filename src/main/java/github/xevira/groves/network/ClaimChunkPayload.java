package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.ChunkPos;

public record ClaimChunkPayload(ChunkPos pos) implements CustomPayload {
    public static final Id<ClaimChunkPayload> ID = new Id<>(Groves.id("claim_chunk"));
    public static final PacketCodec<RegistryByteBuf, ClaimChunkPayload> PACKET_CODEC = PacketCodec.tuple(
            ChunkPos.PACKET_CODEC, ClaimChunkPayload::pos,
            ClaimChunkPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
