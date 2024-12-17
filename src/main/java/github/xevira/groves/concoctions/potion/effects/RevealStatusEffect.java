package github.xevira.groves.concoctions.potion.effects;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

/** Strips off Invisibility off entities  **/
public class RevealStatusEffect extends InstantStatusEffectBase {
    public RevealStatusEffect() {
        super(StatusEffectCategory.NEUTRAL, 0xC0C0C0);
    }

    @Override
    public void applyInstantEffect(ServerWorld world, @Nullable Entity effectEntity, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity) {
        target.removeStatusEffect(StatusEffects.INVISIBILITY);
    }
}
