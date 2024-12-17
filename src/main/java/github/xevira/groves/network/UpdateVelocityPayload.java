package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.Vec3d;

public record UpdateVelocityPayload(int id, Vec3d velocity) implements CustomPayload {
    public static final Id<UpdateVelocityPayload> ID = new Id<>(Groves.id("update_velocity"));
    public static final PacketCodec<RegistryByteBuf, UpdateVelocityPayload> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, UpdateVelocityPayload::id,
            Vec3d.PACKET_CODEC, UpdateVelocityPayload::velocity,
            UpdateVelocityPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
