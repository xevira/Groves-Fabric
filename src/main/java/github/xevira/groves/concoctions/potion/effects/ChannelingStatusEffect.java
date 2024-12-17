package github.xevira.groves.concoctions.potion.effects;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;

/** Strikes the entity with lightning randomly during thunderstorms and are not beneath cover. **/
public class ChannelingStatusEffect extends StatusEffectBase {
    public ChannelingStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0xBFFFFF, 100, true, ParticleTypes.ELECTRIC_SPARK);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        if (world.isThundering())
        {
            // TODO: make the 2.5% configurable
            if (world.isSkyVisible(entity.getBlockPos()) && world.random.nextDouble() < 0.025)
            {
                EntityType.LIGHTNING_BOLT.spawn(world, entity.getBlockPos(), SpawnReason.TRIGGERED);
            }
        }

        return true;
    }
}
