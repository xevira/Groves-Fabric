package github.xevira.groves.concoctions.potion.effects;

import net.minecraft.entity.effect.StatusEffectCategory;

/** Attempts to convert the entity into one of its variants **/
public class ConversionStatusEffect extends InstantStatusEffectBase {
    public ConversionStatusEffect() {
        super(StatusEffectCategory.NEUTRAL, 0xFF00FF);
    }
}
