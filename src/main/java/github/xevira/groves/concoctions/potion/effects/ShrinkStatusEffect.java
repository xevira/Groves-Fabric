package github.xevira.groves.concoctions.potion.effects;

import github.xevira.groves.Groves;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectCategory;

public class ShrinkStatusEffect extends StatusEffectBase {
    public ShrinkStatusEffect() {
        super(StatusEffectCategory.NEUTRAL, 0xFF8040, 0, false);

        addAttributeModifier(EntityAttributes.SCALE, Groves.id("shrink"), -0.1, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }
}