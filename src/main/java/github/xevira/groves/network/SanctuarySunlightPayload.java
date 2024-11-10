package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record SanctuarySunlightPayload(int sunlightPercent) implements CustomPayload {
    public static final Id<SanctuarySunlightPayload> ID = new Id<>(Groves.id("sanctuary_sunlight"));
    public static final PacketCodec<RegistryByteBuf, SanctuarySunlightPayload> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, SanctuarySunlightPayload::sunlightPercent,
            SanctuarySunlightPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
