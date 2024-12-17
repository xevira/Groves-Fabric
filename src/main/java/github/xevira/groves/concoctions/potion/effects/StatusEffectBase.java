package github.xevira.groves.concoctions.potion.effects;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.particle.ParticleEffect;

public abstract class StatusEffectBase extends StatusEffect {
    private final int tickRate;
    private final boolean fasterWithAmplify;

    protected StatusEffectBase(StatusEffectCategory category, int color, int tickRate, boolean faster) {
        super(category, color);

        this.tickRate = tickRate;
        this.fasterWithAmplify = faster;
    }

    protected StatusEffectBase(StatusEffectCategory category, int color, int tickRate, boolean faster, ParticleEffect effect) {
        super(category, color, effect);

        this.tickRate = tickRate;
        this.fasterWithAmplify = faster;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        if (this.tickRate <= 0)
            return false;

        int rate = this.fasterWithAmplify ? (this.tickRate >> amplifier) : this.tickRate;

        if (rate > 1)
            return (duration % rate) == 0;

        return true;
    }
}
