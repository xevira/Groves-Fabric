package github.xevira.groves.concoctions.potion.effects;

import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

public class InfernoStatusEffect extends StatusEffectBase {
    public InfernoStatusEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0xFF6F00, 0, false, ParticleTypes.FLAME);
    }
}
