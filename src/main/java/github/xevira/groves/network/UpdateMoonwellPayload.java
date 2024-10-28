package github.xevira.groves.network;

import github.xevira.groves.Groves;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public record UpdateMoonwellPayload(@Nullable BlockPos pos) implements CustomPayload {
    public static final Id<UpdateMoonwellPayload> ID = new Id<>(Groves.id("update_moonwell"));
    public static final PacketCodec<RegistryByteBuf, UpdateMoonwellPayload> PACKET_CODEC = new PacketCodec<RegistryByteBuf, UpdateMoonwellPayload>() {
        @Override
        public UpdateMoonwellPayload decode(RegistryByteBuf buf) {
            if (PacketCodecs.BOOL.decode(buf))
                return new UpdateMoonwellPayload(BlockPos.PACKET_CODEC.decode(buf));
            else
                return new UpdateMoonwellPayload(null);
        }

        @Override
        public void encode(RegistryByteBuf buf, UpdateMoonwellPayload value) {
            if (value.pos() != null)
            {
                PacketCodecs.BOOL.encode(buf, true);
                BlockPos.PACKET_CODEC.encode(buf, value.pos());
            }
            else
                PacketCodecs.BOOL.encode(buf, false);
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
