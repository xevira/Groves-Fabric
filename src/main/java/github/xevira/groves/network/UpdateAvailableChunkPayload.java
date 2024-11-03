package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.ChunkPos;

public record UpdateAvailableChunkPayload(ChunkPos pos, boolean add) implements CustomPayload {
    public static final Id<UpdateAvailableChunkPayload> ID = new Id<>(Groves.id("update_available_chunk"));
    public static final PacketCodec<RegistryByteBuf, UpdateAvailableChunkPayload> PACKET_CODEC = PacketCodec.tuple(
            ChunkPos.PACKET_CODEC, UpdateAvailableChunkPayload::pos,
            PacketCodecs.BOOL, UpdateAvailableChunkPayload::add,
            UpdateAvailableChunkPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
