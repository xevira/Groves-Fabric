package github.xevira.groves.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import github.xevira.groves.fluid.FluidSystem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean grovesTick(boolean original) {
        if(original)
            return true;

        return FluidSystem.FLUIDS.values().stream()
                .filter(FluidSystem::shouldTurtleHelmetActivate)
                .anyMatch(data -> isSubmergedIn(data.fluidTag()));
    }

    @ModifyExpressionValue(method = "getBlockBreakingSpeed",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean grovesGetBlockBreakingSpeed(boolean original) {
        if(original)
            return true;

        return FluidSystem.FLUIDS.values().stream()
                .filter(FluidSystem::affectsBlockBreakSpeed)
                .anyMatch(data -> isSubmergedIn(data.fluidTag()));
    }
}