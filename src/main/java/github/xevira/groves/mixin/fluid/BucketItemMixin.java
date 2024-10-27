package github.xevira.groves.mixin.fluid;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import github.xevira.groves.Registration;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BucketItem.class)
public class BucketItemMixin {
    @Shadow
    @Final
    private Fluid fluid;

    @ModifyExpressionValue(method = "placeFluid",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/Fluid;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    @SuppressWarnings("deprecation") // We're just doing what vanilla does
    private boolean groves$placeFluid(boolean original) {
        if(original)
            return true;

        return this.fluid.isIn(Registration.BLESSED_MOON_WATERS_TAG) ||
                this.fluid.isIn(Registration.MOONLIGHT_TAG);
    }
}