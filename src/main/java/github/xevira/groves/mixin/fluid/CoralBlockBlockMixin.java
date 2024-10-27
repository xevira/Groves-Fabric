package github.xevira.groves.mixin.fluid;

import github.xevira.groves.fluid.FluidSystem;
import net.minecraft.block.CoralBlockBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CoralBlockBlock.class)
public class CoralBlockBlockMixin {
    @Inject(method="isInWater", at=@At("HEAD"), cancellable=true)
    private void grovesIsInWater(BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> callback) {
        FluidSystem fluidSystem = FluidSystem.FLUIDS.get(world.getFluidState(pos).getFluid());
        if (fluidSystem != null && fluidSystem.canCoralSurvive()) {
            callback.setReturnValue(true);
        }
    }
}