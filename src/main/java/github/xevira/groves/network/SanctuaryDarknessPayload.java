package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record SanctuaryDarknessPayload(int darknessPercent) implements CustomPayload {
    public static final Id<SanctuaryDarknessPayload> ID = new Id<>(Groves.id("sanctuary_darkness"));
    public static final PacketCodec<RegistryByteBuf, SanctuaryDarknessPayload> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, SanctuaryDarknessPayload::darknessPercent,
            SanctuaryDarknessPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
