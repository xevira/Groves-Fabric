package github.xevira.groves.concoctions.potion.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/** Restores saturation then hunger while in the sunlight. **/
public class PhotosynthesisStatusEffect extends StatusEffectBase {
    public PhotosynthesisStatusEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0x86EB34, 100, true);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayerEntity player)
        {
            if (world.isDay() && world.isSkyVisible(player.getBlockPos()) && !world.isRaining() && !world.isThundering())
            {
                HungerManager hungerManager = player.getHungerManager();

                if (hungerManager.isNotFull())
                    hungerManager.add(1, 0.0f);
                else
                    hungerManager.add(0, 0.1f);
            }
        }
        return true;
    }
}
