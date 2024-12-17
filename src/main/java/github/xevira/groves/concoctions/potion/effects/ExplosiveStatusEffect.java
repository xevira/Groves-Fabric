package github.xevira.groves.concoctions.potion.effects;

import github.xevira.groves.Registration;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class ExplosiveStatusEffect extends StatusEffectBase {
    public ExplosiveStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0xFF0000, 0, false);
    }

    @Override
    public void onEntityDamage(ServerWorld world, LivingEntity entity, int amplifier, DamageSource source, float amount) {
        if (source.getAttacker() != entity)
        {
            // TODO: Make the 3.0f a configuration option
            world.createExplosion(entity, entity.getX(), entity.getY(), entity.getZ(), (float)(amplifier + 1) * 3.0f, World.ExplosionSourceType.MOB);
        }
    }
}
