package github.xevira.groves.concoctions.potion.effects;

import net.minecraft.entity.effect.StatusEffectCategory;

/** Causes entity to bounce **/
public class BouncyStatusEffect extends StatusEffectBase {
    public BouncyStatusEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0x10E325, 0, false);
    }
}
