package github.xevira.groves.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import github.xevira.groves.fluid.FluidSystem;
import net.minecraft.entity.ExperienceOrbEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ExperienceOrbEntity.class)
public class ExperienceOrbEntityMixin {
    @ModifyExpressionValue(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean grovesTick(boolean original) {
        if (original)
            return true;

        return FluidSystem.FLUIDS.values().stream().anyMatch(FluidSystem::applyWaterMovement);
    }
}