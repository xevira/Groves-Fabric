package github.xevira.groves.concoctions.potion.effects;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

/** Makes the entity silent **/
public class SilenceStatusEffect extends InstantStatusEffectBase {
    public SilenceStatusEffect() {
        super(StatusEffectCategory.NEUTRAL, 0x000000);
    }

    @Override
    public void applyInstantEffect(ServerWorld world, @Nullable Entity effectEntity, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity) {
        target.setSilent(true);
    }
}
