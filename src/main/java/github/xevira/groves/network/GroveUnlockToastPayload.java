package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record GroveUnlockToastPayload(String unlock) implements CustomPayload {
    public static final Id<GroveUnlockToastPayload> ID = new Id<>(Groves.id("unlock_toast"));
    public static final PacketCodec<RegistryByteBuf, GroveUnlockToastPayload> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, GroveUnlockToastPayload::unlock,
            GroveUnlockToastPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
