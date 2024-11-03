package github.xevira.groves.network;

import github.xevira.groves.Groves;
import github.xevira.groves.poi.GrovesPOI;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.ChunkPos;

import java.util.HashSet;
import java.util.Set;

// Sends the entire list
public record ResetAvailableChunksPayload(Set<ChunkPos> chunks) implements CustomPayload {
    public static final Id<ResetAvailableChunksPayload> ID = new Id<>(Groves.id("reset_available_chunks"));
    public static final PacketCodec<RegistryByteBuf, ResetAvailableChunksPayload> PACKET_CODEC = new PacketCodec<RegistryByteBuf, ResetAvailableChunksPayload>() {
        @Override
        public ResetAvailableChunksPayload decode(RegistryByteBuf buf) {
            int chunks = PacketCodecs.INTEGER.decode(buf);
            Set<ChunkPos> data = new HashSet<>();
            for(int i = 0; i < chunks; i++)
            {
                data.add(ChunkPos.PACKET_CODEC.decode(buf));
            }
            return new ResetAvailableChunksPayload(data);
        }

        @Override
        public void encode(RegistryByteBuf buf, ResetAvailableChunksPayload value) {
            PacketCodecs.INTEGER.encode(buf, value.chunks().size());

            for(ChunkPos pos : value.chunks())
            {
                ChunkPos.PACKET_CODEC.encode(buf, pos);
            }
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
