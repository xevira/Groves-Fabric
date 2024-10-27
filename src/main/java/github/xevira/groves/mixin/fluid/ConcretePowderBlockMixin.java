package github.xevira.groves.mixin.fluid;

import github.xevira.groves.fluid.FluidSystem;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConcretePowderBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ConcretePowderBlock.class)
public class ConcretePowderBlockMixin {
    @Inject(method="hardensIn", at=@At("HEAD"), cancellable=true)
    private static void grovesHardensIn(BlockState state, CallbackInfoReturnable<Boolean> callback) {
        FluidSystem fluidSystem = FluidSystem.FLUIDS.get(state.getFluidState().getFluid());
        if (fluidSystem != null && fluidSystem.hardensConcrete()) {
            callback.setReturnValue(true);
        }
    }
}