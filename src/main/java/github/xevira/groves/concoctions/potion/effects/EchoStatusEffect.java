package github.xevira.groves.concoctions.potion.effects;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

/** Makes entities no longer silent **/
public class EchoStatusEffect extends InstantStatusEffectBase {
    public EchoStatusEffect() {
        super(StatusEffectCategory.NEUTRAL, 0x0A5060);
    }

    @Override
    public void applyInstantEffect(ServerWorld world, @Nullable Entity effectEntity, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity) {
        target.setSilent(false);
    }
}
