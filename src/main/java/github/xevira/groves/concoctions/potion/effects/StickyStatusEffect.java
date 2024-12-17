package github.xevira.groves.concoctions.potion.effects;

import github.xevira.groves.Registration;
import github.xevira.groves.network.UpdateVelocityPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

/**
 * <p>Allows entity to stick to walls and ceilings.  It also makes them stick to floors, too.</p>
 *
 * <p>Will not function if under the effect of Spider Walking or Gravity.</p>
 * **/
public class StickyStatusEffect extends StatusEffectBase {
    public StickyStatusEffect() {
        super(StatusEffectCategory.NEUTRAL, 0xcc8100, 1, false);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        if (!entity.hasStatusEffect(Registration.SPIDER_WALKING_STATUS_EFFECT) &&
            !entity.hasStatusEffect(Registration.GRAVITY_STATUS_EFFECT))
        {
            if (entity.horizontalCollision || entity.verticalCollision)
            {
                Vec3d motion = entity.getVelocity();
                double f = 1.0 - 0.02 * (amplifier + 1);
                entity.setVelocity(new Vec3d(motion.x * f, 0.0D, motion.z * f));
                entity.velocityDirty = true;
                entity.fallDistance = 0;

                UpdateVelocityPayload payload = new UpdateVelocityPayload(entity.getId(), entity.getVelocity());
                for(ServerPlayerEntity player : world.getPlayers())
                {
                    ServerPlayNetworking.send(player, payload);
                }

            }
        }
        return true;
    }
}
