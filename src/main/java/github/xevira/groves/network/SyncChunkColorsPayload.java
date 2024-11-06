package github.xevira.groves.network;

import github.xevira.groves.Groves;
import github.xevira.groves.poi.GrovesPOI;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record SyncChunkColorsPayload(String worldId, List<ChunkPos> chunks, GrovesPOI.ClientGroveSanctuaryColorData colors) implements CustomPayload {
    public static final Id<SyncChunkColorsPayload> ID = new Id<>(Groves.id("sync_chunk_colors"));
    public static final PacketCodec<RegistryByteBuf, SyncChunkColorsPayload> PACKET_CODEC = new PacketCodec<RegistryByteBuf, SyncChunkColorsPayload>() {
        @Override
        public SyncChunkColorsPayload decode(RegistryByteBuf buf) {
            String worldId = PacketCodecs.STRING.decode(buf);
            int count = PacketCodecs.INTEGER.decode(buf);
            List<ChunkPos> chunks = new ArrayList<>();
            for(int i = 0; i < count; i++)
            {
                ChunkPos pos = ChunkPos.PACKET_CODEC.decode(buf);

                chunks.add(pos);
            }
            GrovesPOI.ClientGroveSanctuaryColorData colors = GrovesPOI.ClientGroveSanctuaryColorData.PACKET_CODEC.decode(buf);

            return new SyncChunkColorsPayload(worldId, chunks, colors);
        }

        @Override
        public void encode(RegistryByteBuf buf, SyncChunkColorsPayload value) {
            PacketCodecs.STRING.encode(buf, value.worldId());
            PacketCodecs.INTEGER.encode(buf, value.chunks().size());
            value.chunks().forEach(chunk-> {
                ChunkPos.PACKET_CODEC.encode(buf, chunk);
            });
            GrovesPOI.ClientGroveSanctuaryColorData.PACKET_CODEC.encode(buf, value.colors());
        }
    };


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
