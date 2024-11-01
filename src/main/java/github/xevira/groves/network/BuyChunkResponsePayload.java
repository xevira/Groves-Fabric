package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.math.ChunkPos;

public record BuyChunkResponsePayload(ChunkPos pos, boolean success, Text reason) implements CustomPayload {
    public static final Id<BuyChunkResponsePayload> ID = new Id<>(Groves.id("buy_chunk_response"));
    public static final PacketCodec<RegistryByteBuf, BuyChunkResponsePayload> PACKET_CODEC = PacketCodec.tuple(
            ChunkPos.PACKET_CODEC, BuyChunkResponsePayload::pos,
            PacketCodecs.BOOL, BuyChunkResponsePayload::success,
            TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC, BuyChunkResponsePayload::reason,
            BuyChunkResponsePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
