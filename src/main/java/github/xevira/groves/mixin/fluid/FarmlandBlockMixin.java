package github.xevira.groves.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import github.xevira.groves.fluid.FluidSystem;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FarmlandBlock.class)
public class FarmlandBlockMixin {
    @ModifyExpressionValue(method = "isWaterNearby",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private static boolean grovesIsWaterNearby(boolean original, WorldView world, BlockPos pos) {
        if(original)
            return true;

        FluidSystem fluidSystem = FluidSystem.FLUIDS.get(world.getFluidState(pos).getFluid());
        return fluidSystem != null && fluidSystem.canMoisturizeFarmland();
    }
}