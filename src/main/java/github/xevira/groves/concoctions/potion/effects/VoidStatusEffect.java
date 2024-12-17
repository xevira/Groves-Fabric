package github.xevira.groves.concoctions.potion.effects;

import github.xevira.groves.Registration;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.world.ServerWorld;

/** Gives void-like damage to entity **/
public class VoidStatusEffect extends StatusEffectBase {
    public VoidStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0x2b152b, 40, true);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        entity.damage(world, entity.getDamageSources().create(Registration.VOID_DAMAGE), 1.0f);
        return true;
    }
}
