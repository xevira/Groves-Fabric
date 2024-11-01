package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.ChunkPos;

public record BuyChunkPayload(ChunkPos pos) implements CustomPayload {
    public static final Id<BuyChunkPayload> ID = new Id<>(Groves.id("buy_chunk"));
    public static final PacketCodec<RegistryByteBuf, BuyChunkPayload> PACKET_CODEC = PacketCodec.tuple(
            ChunkPos.PACKET_CODEC, BuyChunkPayload::pos,
            BuyChunkPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
