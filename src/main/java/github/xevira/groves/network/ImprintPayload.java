package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record ImprintPayload() implements CustomPayload {
    public static final Id<ImprintPayload> ID = new Id<>(Groves.id("imprint"));
    public static final PacketCodec<RegistryByteBuf, ImprintPayload> PACKET_CODEC = new PacketCodec<RegistryByteBuf, ImprintPayload>() {
        @Override
        public ImprintPayload decode(RegistryByteBuf buf) {
            return new ImprintPayload();
        }

        @Override
        public void encode(RegistryByteBuf buf, ImprintPayload value) {
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
