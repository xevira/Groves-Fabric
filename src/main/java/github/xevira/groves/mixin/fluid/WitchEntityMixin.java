package github.xevira.groves.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import github.xevira.groves.fluid.FluidSystem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WitchEntity.class)
public abstract class WitchEntityMixin extends RaiderEntity {
    protected WitchEntityMixin(EntityType<? extends RaiderEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(method = "tickMovement",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/mob/WitchEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean grovesTickMovement(boolean original) {
        if(original)
            return true;

        return FluidSystem.FLUIDS.values().stream()
                .filter(FluidSystem::shouldWitchDrinkWaterBreathing)
                .anyMatch(fluidData -> isSubmergedIn(fluidData.fluidTag()));
    }
}