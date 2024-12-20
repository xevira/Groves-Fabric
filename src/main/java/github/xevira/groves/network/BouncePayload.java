package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.Vec3d;

public record BouncePayload(int id, Vec3d velocity) implements CustomPayload {
    public static final Id<BouncePayload> ID = new Id<>(Groves.id("bounce"));
    public static final PacketCodec<RegistryByteBuf, BouncePayload> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, BouncePayload::id,
            Vec3d.PACKET_CODEC, BouncePayload::velocity,
            BouncePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
