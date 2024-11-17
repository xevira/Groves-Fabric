package github.xevira.groves.network;

import github.xevira.groves.Groves;
import github.xevira.groves.sanctuary.ClientGroveSanctuary;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record GrovesSanctuaryScreenPayload(ClientGroveSanctuary sanctuary) implements CustomPayload {
    public static final Id<GrovesSanctuaryScreenPayload> ID = new Id<>(Groves.id("groves_sanctuary"));
    public static final PacketCodec<RegistryByteBuf, GrovesSanctuaryScreenPayload> PACKET_CODEC = new PacketCodec<RegistryByteBuf, GrovesSanctuaryScreenPayload>() {
        @Override
        public GrovesSanctuaryScreenPayload decode(RegistryByteBuf buf) {
            return new GrovesSanctuaryScreenPayload(ClientGroveSanctuary.PACKET_CODEC.decode(buf));
        }

        @Override
        public void encode(RegistryByteBuf buf, GrovesSanctuaryScreenPayload value) {
            ClientGroveSanctuary.PACKET_CODEC.encode(buf, value.sanctuary());
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
