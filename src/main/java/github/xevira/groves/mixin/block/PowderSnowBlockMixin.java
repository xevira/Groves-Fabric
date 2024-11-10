package github.xevira.groves.mixin.block;

import github.xevira.groves.Registration;
import github.xevira.groves.util.EnchantHelper;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PowderSnowBlock.class)
abstract class PowderSnowBlockMixin {

    @Inject(method = "canWalkOnPowderSnow(Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private static void canWalkOnPowderSnow(Entity entity, CallbackInfoReturnable<Boolean> clr)
    {
       if (entity instanceof LivingEntity living)
       {
            if (EnchantHelper.getEnchantLevel(living.getEquippedStack(EquipmentSlot.FEET), Registration.LIGHT_FOOTED_ENCHANTMENT_KEY) > 0)
            {
                clr.setReturnValue(true);
            }
       }
    }
}
