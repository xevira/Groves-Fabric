package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record SetGroveNameResponsePayload(String name, boolean success, Text reason) implements CustomPayload {
    public static final Id<SetGroveNameResponsePayload> ID = new Id<>(Groves.id("set_grove_name"));
    public static final PacketCodec<RegistryByteBuf, SetGroveNameResponsePayload> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, SetGroveNameResponsePayload::name,
            PacketCodecs.BOOLEAN, SetGroveNameResponsePayload::success,
            TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC, SetGroveNameResponsePayload::reason,
            SetGroveNameResponsePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
