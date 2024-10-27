package github.xevira.groves.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import github.xevira.groves.fluid.FluidSystem;
import net.minecraft.block.BlockState;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// TODO: Blast resistance
@Mixin(RespawnAnchorBlock.class)
public class RespawnAnchorBlockMixin {
    @ModifyExpressionValue(method = "hasStillWater",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z",
                    ordinal = 0))
    private static boolean grovesHasStillWater1(boolean original, BlockPos pos, World world, @Local FluidState fluidState) {
        if(original)
            return true;

        FluidSystem fluidSystem = FluidSystem.FLUIDS.get(fluidState.getFluid());
        return fluidSystem == null || !fluidSystem.affectsRespawnAnchor();
    }

    @ModifyExpressionValue(method = "hasStillWater",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z",
                    ordinal = 1))
    private static boolean grovesHasStillWater2(boolean original, BlockPos pos, World world, @Local(ordinal = 1) FluidState fluidState) {
        if(original)
            return true;

        FluidSystem fluidSystem = FluidSystem.FLUIDS.get(fluidState.getFluid());
        return fluidSystem == null || !fluidSystem.affectsRespawnAnchor();
    }

    @ModifyExpressionValue(method = "explode",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean grovesExplode(boolean original, BlockState state, World world, BlockPos explodedPos) {
        if (original)
            return true;

        FluidSystem fluidSystem = FluidSystem.FLUIDS.get(state.getFluidState().getFluid());
        return fluidSystem != null && fluidSystem.affectsRespawnAnchor();
    }
}