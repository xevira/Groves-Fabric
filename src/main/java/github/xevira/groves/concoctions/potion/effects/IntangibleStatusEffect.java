package github.xevira.groves.concoctions.potion.effects;

import github.xevira.groves.Groves;
import github.xevira.groves.Registration;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffectCategory;

/** Makes players able to pass through blocks **/
public class IntangibleStatusEffect extends StatusEffectBase {
    public IntangibleStatusEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0x999999, 0, false);

        addAttributeModifier(Registration.INTANGIBLE_ATTRIBUTE,
                Groves.id("intangible"),
                1.0D,
                EntityAttributeModifier.Operation.ADD_VALUE);
    }
}
