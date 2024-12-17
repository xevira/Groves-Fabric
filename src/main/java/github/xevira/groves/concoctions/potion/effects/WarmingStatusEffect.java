package github.xevira.groves.concoctions.potion.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.world.ServerWorld;

/** Prevents entity from becoming frozen and taking freezing damage **/
// TODO: see about making it have an adverse affect when taking fire damage
public class WarmingStatusEffect extends StatusEffectBase {
    public WarmingStatusEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0xF08C5D, 1, false);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        entity.setFrozenTicks(0);
        return true;
    }
}
