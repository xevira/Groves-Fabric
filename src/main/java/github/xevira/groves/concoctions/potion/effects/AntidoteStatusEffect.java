package github.xevira.groves.concoctions.potion.effects;

import net.minecraft.entity.effect.StatusEffectCategory;

/** Makes the player immune to poison damage **/
public class AntidoteStatusEffect extends StatusEffectBase {
    public AntidoteStatusEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0x1DAD10, 0, false);
    }
}
