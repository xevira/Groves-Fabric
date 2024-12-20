package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.ChunkPos;

public record SetChunkLoadingPayload(ChunkPos pos, boolean loaded) implements CustomPayload {
    public static final Id<SetChunkLoadingPayload> ID = new Id<>(Groves.id("set_chunk_loading"));
    public static final PacketCodec<RegistryByteBuf, SetChunkLoadingPayload> PACKET_CODEC = PacketCodec.tuple(
            ChunkPos.PACKET_CODEC, SetChunkLoadingPayload::pos,
            PacketCodecs.BOOLEAN, SetChunkLoadingPayload::loaded,
            SetChunkLoadingPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
