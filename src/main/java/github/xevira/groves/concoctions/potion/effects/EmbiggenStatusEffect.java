package github.xevira.groves.concoctions.potion.effects;

import github.xevira.groves.Groves;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectCategory;

public class EmbiggenStatusEffect extends StatusEffectBase {
    public EmbiggenStatusEffect() {
        super(StatusEffectCategory.NEUTRAL, 0x4080FF, 0, false);

        addAttributeModifier(EntityAttributes.SCALE, Groves.id("embiggen"), 0.1, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }
}
