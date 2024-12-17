package github.xevira.groves.concoctions.potion.effects;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;
import org.jetbrains.annotations.Nullable;

/** Teleports player back to their respawn point **/
public class RecallStatusEffect extends InstantStatusEffectBase {
    public RecallStatusEffect() {
        super(StatusEffectCategory.NEUTRAL, 0x00b5bc);
    }

    @Override
    public void applyInstantEffect(ServerWorld world, @Nullable Entity effectEntity, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity) {
        if (target instanceof ServerPlayerEntity player)
        {
            TeleportTarget location = player.getRespawnTarget(true, TeleportTarget.NO_OP);
            player.teleportTo(location);
        }

        super.applyInstantEffect(world, effectEntity, attacker, target, amplifier, proximity);
    }
}
