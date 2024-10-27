package github.xevira.groves.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import github.xevira.groves.fluid.FluidSystem;
import net.minecraft.block.SpongeBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SpongeBlock.class)
public class SpongeBlockMixin {
    @ModifyExpressionValue(method = "method_49829",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private static boolean grovesAbsorbWater(boolean original, BlockPos pos, World world, BlockPos currentPos, @Local FluidState fluidState) {
        if (original)
            return true;

        FluidSystem data = FluidSystem.FLUIDS.get(fluidState.getFluid());
        return data == null || !data.canSpongeAbsorb();
    }
}