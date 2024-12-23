package github.xevira.groves.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import github.xevira.groves.fluid.FluidSystem;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuardianEntity.class)
public class GuardianEntityMixin {
    @ModifyExpressionValue(method = "getPathfindingFavor",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean grovesGetPathfindingFavor(boolean original, BlockPos pos, WorldView world) {
        if(original)
            return true;

        FluidState fluidState = world.getFluidState(pos);
        return FluidSystem.FLUIDS.values().stream()
                .filter(FluidSystem::canGuardianSpawn)
                .anyMatch(data -> fluidState.isIn(data.fluidTag()));
    }
}