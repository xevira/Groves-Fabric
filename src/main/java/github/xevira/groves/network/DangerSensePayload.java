package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record DangerSensePayload() implements CustomPayload {
    public static final Id<DangerSensePayload> ID = new Id<>(Groves.id("danger_sense"));
    public static final PacketCodec<RegistryByteBuf, DangerSensePayload> PACKET_CODEC = PacketCodec.unit(new DangerSensePayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
