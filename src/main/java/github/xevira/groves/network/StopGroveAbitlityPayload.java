package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record StopGroveAbitlityPayload(String name) implements CustomPayload {
    public static final Id<StopGroveAbitlityPayload> ID = new Id<>(Groves.id("stop_grove_ability"));
    public static final PacketCodec<RegistryByteBuf, StopGroveAbitlityPayload> PACKET_CODEC =
            PacketCodec.tuple(PacketCodecs.STRING, StopGroveAbitlityPayload::name, StopGroveAbitlityPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
