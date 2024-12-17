package github.xevira.groves.concoctions.potion.effects;

import github.xevira.groves.Groves;
import net.minecraft.entity.EntityAttachments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectCategory;

/** Increases knockback resistance **/
public class AnchorStatusEffect extends StatusEffectBase {
    public AnchorStatusEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0x332B45, 0, false);

        addAttributeModifier(EntityAttributes.KNOCKBACK_RESISTANCE,
                Groves.id("anchor"),
                0.1f,
                EntityAttributeModifier.Operation.ADD_VALUE);
    }
}
