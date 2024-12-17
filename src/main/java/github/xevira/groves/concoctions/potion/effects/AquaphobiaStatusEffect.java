package github.xevira.groves.concoctions.potion.effects;

import github.xevira.groves.Registration;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.world.ServerWorld;

/** Causes you to take damage if exposed to water or rain. **/
public class AquaphobiaStatusEffect extends StatusEffectBase {
    public AquaphobiaStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0x55CEE0, 20, false);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        if (entity.isTouchingWaterOrRain())
        {
            entity.damage(world, entity.getDamageSources().create(Registration.WATER_DAMAGE), (amplifier + 1));
        }

        return true;
    }
}
