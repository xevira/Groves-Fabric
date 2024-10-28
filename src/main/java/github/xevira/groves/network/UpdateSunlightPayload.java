package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record UpdateSunlightPayload(long sunlight) implements CustomPayload {
    public static final Id<UpdateSunlightPayload> ID = new Id<>(Groves.id("update_sunlight"));
    public static final PacketCodec<RegistryByteBuf, UpdateSunlightPayload> PACKET_CODEC =
            PacketCodec.tuple(PacketCodecs.LONG, UpdateSunlightPayload::sunlight, UpdateSunlightPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
