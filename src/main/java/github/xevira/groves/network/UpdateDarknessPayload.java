package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record UpdateDarknessPayload(int darkness, long total) implements CustomPayload {
    public static final Id<UpdateDarknessPayload> ID = new Id<>(Groves.id("update_darkness"));
    public static final PacketCodec<RegistryByteBuf, UpdateDarknessPayload> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, UpdateDarknessPayload::darkness,
            PacketCodecs.LONG, UpdateDarknessPayload::total,
            UpdateDarknessPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
