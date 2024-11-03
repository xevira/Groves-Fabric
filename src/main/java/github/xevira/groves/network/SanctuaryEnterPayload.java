package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record SanctuaryEnterPayload(UUID uuid, String name, String groveName, boolean abandoned, boolean entry) implements CustomPayload {
    public static final Id<SanctuaryEnterPayload> ID = new Id<>(Groves.id("enter_sanctuary"));
    public static final PacketCodec<RegistryByteBuf, SanctuaryEnterPayload> PACKET_CODEC = new PacketCodec<RegistryByteBuf, SanctuaryEnterPayload>() {
        @Override
        public SanctuaryEnterPayload decode(RegistryByteBuf buf) {
            boolean entry = PacketCodecs.BOOL.decode(buf);
            boolean abandoned = PacketCodecs.BOOL.decode(buf);
            String groveName= PacketCodecs.STRING.decode(buf);
            UUID uuid;
            String name;
            if (!abandoned) {
                uuid = Uuids.PACKET_CODEC.decode(buf);
                name = PacketCodecs.STRING.decode(buf);
            }
            else {
                uuid = null;
                name = null;
            }
            return new SanctuaryEnterPayload(uuid, name, groveName, abandoned, entry);
        }

        @Override
        public void encode(RegistryByteBuf buf, SanctuaryEnterPayload value) {
            PacketCodecs.BOOL.encode(buf, value.entry);
            PacketCodecs.BOOL.encode(buf, value.abandoned);
            PacketCodecs.STRING.encode(buf, value.groveName);

            if (!value.abandoned) {
                Uuids.PACKET_CODEC.encode(buf, value.uuid);
                PacketCodecs.STRING.encode(buf, value.name);
            }
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
