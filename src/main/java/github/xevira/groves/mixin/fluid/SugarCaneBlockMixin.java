package github.xevira.groves.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import github.xevira.groves.fluid.FluidSystem;
import net.minecraft.block.BlockState;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SugarCaneBlock.class)
public class SugarCaneBlockMixin {
    @ModifyExpressionValue(method = "canPlaceAt",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean grovesCanPlaceAt(boolean original, BlockState state, WorldView world, BlockPos pos, @Local FluidState fluidState) {
        if (original)
            return true;

        FluidSystem data = FluidSystem.FLUIDS.get(fluidState.getFluid());
        return data != null && data.canSugarCaneUse();
    }
}