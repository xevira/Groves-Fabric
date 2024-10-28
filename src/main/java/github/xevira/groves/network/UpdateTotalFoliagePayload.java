package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record UpdateTotalFoliagePayload(int foliage) implements CustomPayload {
    public static final Id<UpdateTotalFoliagePayload> ID = new Id<>(Groves.id("update_total_foliage"));
    public static final PacketCodec<RegistryByteBuf, UpdateTotalFoliagePayload> PACKET_CODEC =
            PacketCodec.tuple(PacketCodecs.INTEGER, UpdateTotalFoliagePayload::foliage, UpdateTotalFoliagePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
