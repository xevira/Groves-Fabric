package github.xevira.groves.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import github.xevira.groves.fluid.FluidSystem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyExpressionValue(method = "baseTick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean grovesBaseTick(boolean original) {
        if (original)
            return true;

        return FluidSystem.FLUIDS.values().stream()
                .filter(FluidSystem::canCauseDrowning)
                .anyMatch(data -> isSubmergedIn(data.fluidTag()));
    }

    @Inject(method = "tickMovement",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;isTouchingWater()Z"))
    private void grovesTickMovement(CallbackInfo callback, @Local(ordinal = 3) LocalDoubleRef g) {
        if (g.get() != 0.0D)
            return;

        for(FluidSystem data : FluidSystem.FLUIDS.values())
        {
            double height = getFluidHeight(data.fluidTag());
            if(height > 0.0D)
            {
                g.set(height);
                break;
            }
        }
    }
}
