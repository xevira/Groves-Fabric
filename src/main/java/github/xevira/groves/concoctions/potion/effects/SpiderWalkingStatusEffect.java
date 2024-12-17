package github.xevira.groves.concoctions.potion.effects;

import github.xevira.groves.Registration;
import github.xevira.groves.network.UpdateVelocityPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

/** Allows entity to climb walls like a spider **/
public class SpiderWalkingStatusEffect extends StatusEffectBase {
    public SpiderWalkingStatusEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0x3d0103, 1, false);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        if (!entity.hasStatusEffect(Registration.GRAVITY_STATUS_EFFECT)) {
            if (entity.horizontalCollision) {
                Vec3d motion = entity.getVelocity();
                Vec3d climbing = new Vec3d(motion.x, 0.2D * (amplifier + 1), motion.z);
                entity.setVelocity(climbing);
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
