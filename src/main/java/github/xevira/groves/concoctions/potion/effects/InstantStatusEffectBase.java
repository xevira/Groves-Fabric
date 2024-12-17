package github.xevira.groves.concoctions.potion.effects;

import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.particle.ParticleEffect;

public abstract class InstantStatusEffectBase extends StatusEffectBase {
    public InstantStatusEffectBase(StatusEffectCategory category, int color) {
        super(category, color, 0, false);
    }

    public InstantStatusEffectBase(StatusEffectCategory category, int color, ParticleEffect effect) {
        super(category, color, 0, false, effect);
    }

    @Override
    public boolean isInstant() {
        return true;
    }
}
