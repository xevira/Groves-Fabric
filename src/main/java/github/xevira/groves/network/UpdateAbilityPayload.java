package github.xevira.groves.network;

import github.xevira.groves.Groves;
import github.xevira.groves.sanctuary.GroveAbility;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record UpdateAbilityPayload(String name, boolean active, long start, long end) implements CustomPayload {
    public static final Id<UpdateAbilityPayload> ID = new Id<>(Groves.id("update_grove_ability"));
    public static final PacketCodec<RegistryByteBuf, UpdateAbilityPayload> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, UpdateAbilityPayload::name,
            PacketCodecs.BOOL, UpdateAbilityPayload::active,
            PacketCodecs.LONG, UpdateAbilityPayload::start,
            PacketCodecs.LONG, UpdateAbilityPayload::end,
            UpdateAbilityPayload::new);

    public UpdateAbilityPayload(GroveAbility ability)
    {
        this(ability.getName(), ability.isActive(), ability.getStartCooldown(), ability.getEndCooldown());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}