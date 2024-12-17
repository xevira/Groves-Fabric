package github.xevira.groves.concoctions.potion.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.world.ServerWorld;

/** Applies the Freezing mechanic as if the entity were in powder snow, provided they are susceptible to freezing damage **/
public class FreezingStatusEffect extends StatusEffectBase {
    public FreezingStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0xAFC8F0, 20, true);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        if (entity.canFreeze()) {
            entity.damage(world, entity.getDamageSources().freeze(), 1.0F);
        }
        return true;
    }
}
