package github.xevira.groves.concoctions.potion.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

/** Pulls the entity down with stronger gravity. **/
public class GravityStatusEffect extends StatusEffectBase {
    public GravityStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0x7998d7, 10, false);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {

        if (entity instanceof PlayerEntity player)
        {
            if (player.isCreative() && player.getAbilities().flying)
                return true;

        }

        if (!entity.isOnGround()) {
            Vec3d motion = entity.getVelocity();

            motion = motion.subtract(0.0D, 0.12D * (amplifier + 1), 0.0D);

            entity.setVelocity(motion);
        }

        return true;
    }
}
