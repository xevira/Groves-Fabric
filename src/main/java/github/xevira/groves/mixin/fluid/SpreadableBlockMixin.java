package github.xevira.groves.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import github.xevira.groves.fluid.FluidSystem;
import net.minecraft.block.BlockState;
import net.minecraft.block.SpreadableBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SpreadableBlock.class)
public class SpreadableBlockMixin {
    @ModifyExpressionValue(method = "canSpread",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private static boolean grovesCanSpread(boolean original, BlockState state, WorldView world, BlockPos pos, @Local(ordinal = 1) BlockPos upPos) {
        if (original)
            return true;

        FluidSystem data = FluidSystem.FLUIDS.get(world.getFluidState(upPos).getFluid());
        return data == null || !data.preventsBlockSpreading();
    }
}