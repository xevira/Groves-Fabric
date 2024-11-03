package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record SetGroveNamePayload(String name) implements CustomPayload {
    public static final Id<SetGroveNamePayload> ID = new Id<>(Groves.id("set_grove_name"));
    public static final PacketCodec<RegistryByteBuf, SetGroveNamePayload> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, SetGroveNamePayload::name,
            SetGroveNamePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
