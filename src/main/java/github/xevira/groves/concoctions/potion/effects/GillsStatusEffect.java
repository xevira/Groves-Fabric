package github.xevira.groves.concoctions.potion.effects;

import net.minecraft.entity.effect.StatusEffectCategory;

/** Allows entity to breathe underwater, at the expense of breathing air. **/
public class GillsStatusEffect extends StatusEffectBase {
    public GillsStatusEffect() {
        super(StatusEffectCategory.NEUTRAL, 0x1A3C73, 0, false);
    }

    /* TODO: Apply the following rules in mixins
     * 1. While underwater, your air gets reset to 300
     * 2. While out of water, your air will decrement.
     *
     * -  Has no affect on WaterCreatureEntity entities.
     */
}
