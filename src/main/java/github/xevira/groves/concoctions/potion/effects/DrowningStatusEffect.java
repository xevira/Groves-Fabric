package github.xevira.groves.concoctions.potion.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.world.ServerWorld;

/** Causes entity to take drowning damage, regardless if they are underwater or not. **/
public class DrowningStatusEffect extends StatusEffectBase {
    // TODO: See if can add a water drip particle
    public DrowningStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0x0E5956, 20, true);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        entity.damage(world, entity.getDamageSources().drown(), 2.0f);
        return true;
    }
}
