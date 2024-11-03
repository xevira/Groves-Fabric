package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record SetSpawnPointPayload(BlockPos pos) implements CustomPayload {
    public static final Id<SetSpawnPointPayload> ID = new Id<>(Groves.id("set_spawn_point"));
    public static final PacketCodec<RegistryByteBuf, SetSpawnPointPayload> PACKET_CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, SetSpawnPointPayload::pos,
            SetSpawnPointPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
