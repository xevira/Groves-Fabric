package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record BouncePayload() implements CustomPayload {
    public static final Id<BouncePayload> ID = new Id<>(Groves.id("bounce"));
    public static final PacketCodec<RegistryByteBuf, BouncePayload> PACKET_CODEC = PacketCodec.unit(new BouncePayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
