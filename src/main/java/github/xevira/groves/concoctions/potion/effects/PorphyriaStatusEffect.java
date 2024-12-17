package github.xevira.groves.concoctions.potion.effects;

import github.xevira.groves.Registration;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.world.ServerWorld;

/** Causes the entity to take damage when exposed to sunlight. **/
public class PorphyriaStatusEffect extends StatusEffectBase {
    public PorphyriaStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0x851010, 40, true);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        if (world.isDay() && !world.isRaining() && !world.isThundering() && world.isSkyVisible(entity.getBlockPos()))
        {
            entity.damage(world, entity.getDamageSources().create(Registration.SUN_DAMAGE), 1.0f);
        }

        return super.applyUpdateEffect(world, entity, amplifier);
    }
}
