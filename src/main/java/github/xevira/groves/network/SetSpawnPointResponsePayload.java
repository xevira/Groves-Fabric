package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.math.BlockPos;

public record SetSpawnPointResponsePayload(BlockPos pos, boolean success, Text reason) implements CustomPayload {
    public static final Id<SetSpawnPointResponsePayload> ID = new Id<>(Groves.id("set_spawn_point_response"));
    public static final PacketCodec<RegistryByteBuf, SetSpawnPointResponsePayload> PACKET_CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, SetSpawnPointResponsePayload::pos,
            PacketCodecs.BOOL, SetSpawnPointResponsePayload::success,
            TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC, SetSpawnPointResponsePayload::reason,
            SetSpawnPointResponsePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
