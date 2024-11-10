package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record UseGroveAbitlityPayload(String name) implements CustomPayload {
    public static final Id<UseGroveAbitlityPayload> ID = new Id<>(Groves.id("use_grove_ability"));
    public static final PacketCodec<RegistryByteBuf, UseGroveAbitlityPayload> PACKET_CODEC =
            PacketCodec.tuple(PacketCodecs.STRING, UseGroveAbitlityPayload::name, UseGroveAbitlityPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
