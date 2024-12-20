package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record MoonwellScreenPayload(BlockPos pos, boolean day, int phase) implements CustomPayload {
    public static final Id<MoonwellScreenPayload> ID = new Id<>(Groves.id("moonwell_screen"));
    public static final PacketCodec<RegistryByteBuf, MoonwellScreenPayload> PACKET_CODEC = PacketCodec.tuple(
                    BlockPos.PACKET_CODEC, MoonwellScreenPayload::pos,
                    PacketCodecs.BOOLEAN, MoonwellScreenPayload::day,
                    PacketCodecs.INTEGER, MoonwellScreenPayload::phase,
                    MoonwellScreenPayload::new);


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
