package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record OpenGrovesRequestPayload() implements CustomPayload {
    public static final Id<OpenGrovesRequestPayload> ID = new Id<>(Groves.id("open_groves_request"));
    // Packet has no payload
    public static final PacketCodec<RegistryByteBuf, OpenGrovesRequestPayload> PACKET_CODEC = new PacketCodec<>() {
        @Override
        public OpenGrovesRequestPayload decode(RegistryByteBuf buf) {
            return new OpenGrovesRequestPayload();
        }

        @Override
        public void encode(RegistryByteBuf buf, OpenGrovesRequestPayload value) {

        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
