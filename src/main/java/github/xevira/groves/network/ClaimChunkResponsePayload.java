package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.math.ChunkPos;

public record ClaimChunkResponsePayload(ChunkPos pos, boolean success, Text reason) implements CustomPayload {
    public static final Id<ClaimChunkResponsePayload> ID = new Id<>(Groves.id("claim_chunk_response"));
    public static final PacketCodec<RegistryByteBuf, ClaimChunkResponsePayload> PACKET_CODEC = PacketCodec.tuple(
            ChunkPos.PACKET_CODEC, ClaimChunkResponsePayload::pos,
            PacketCodecs.BOOL, ClaimChunkResponsePayload::success,
            TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC, ClaimChunkResponsePayload::reason,
            ClaimChunkResponsePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
