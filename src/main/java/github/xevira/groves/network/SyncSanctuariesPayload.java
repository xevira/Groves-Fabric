package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record SyncSanctuariesPayload(List<UUID> uuids) implements CustomPayload {
    public static final Id<SyncSanctuariesPayload> ID = new Id<>(Groves.id("sync_sanctuaries"));
    public static final PacketCodec<RegistryByteBuf, SyncSanctuariesPayload> PACKET_CODEC = new PacketCodec<RegistryByteBuf, SyncSanctuariesPayload>() {
        @Override
        public SyncSanctuariesPayload decode(RegistryByteBuf buf) {
            int count = PacketCodecs.INTEGER.decode(buf);
            List<UUID> uuids = new ArrayList<>();
            for(int i = 0; i < count; i++)
                uuids.add(Uuids.PACKET_CODEC.decode(buf));
            return new SyncSanctuariesPayload(uuids);
        }

        @Override
        public void encode(RegistryByteBuf buf, SyncSanctuariesPayload value) {
            PacketCodecs.INTEGER.encode(buf, value.uuids.size());
            value.uuids.forEach(uuid -> Uuids.PACKET_CODEC.encode(buf, uuid));
        }
    };

    public SyncSanctuariesPayload(UUID uuid)
    {
        this(List.of(uuid));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
