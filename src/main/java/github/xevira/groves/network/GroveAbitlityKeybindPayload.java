package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record GroveAbitlityKeybindPayload(String name) implements CustomPayload {
    public static final Id<GroveAbitlityKeybindPayload> ID = new Id<>(Groves.id("grove_ability_keybind"));
    public static final PacketCodec<RegistryByteBuf, GroveAbitlityKeybindPayload> PACKET_CODEC =
            PacketCodec.tuple(PacketCodecs.STRING, GroveAbitlityKeybindPayload::name, GroveAbitlityKeybindPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
