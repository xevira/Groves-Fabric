package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record StartGroveAbitlityPayload(String name) implements CustomPayload {
    public static final Id<StartGroveAbitlityPayload> ID = new Id<>(Groves.id("start_grove_ability"));
    public static final PacketCodec<RegistryByteBuf, StartGroveAbitlityPayload> PACKET_CODEC =
            PacketCodec.tuple(PacketCodecs.STRING, StartGroveAbitlityPayload::name, StartGroveAbitlityPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
