package github.xevira.groves.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import github.xevira.groves.fluid.FluidSystem;
import net.minecraft.block.KelpBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KelpBlock.class)
public class KelpBlockMixin {
    @ModifyExpressionValue(method="getPlacementState",
            at=@At(value = "INVOKE",
                    target="Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean grovesGetPlacementState(boolean original, ItemPlacementContext ctx, @Local FluidState fluidState) {
        if(original)
            return true;

        FluidSystem fluidSystem = FluidSystem.FLUIDS.get(fluidState.getFluid());
        return fluidSystem != null && fluidSystem.canKelpSurvive();
    }
}