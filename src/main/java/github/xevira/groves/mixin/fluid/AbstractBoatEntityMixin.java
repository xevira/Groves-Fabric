package github.xevira.groves.mixin.fluid;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import github.xevira.groves.fluid.FluidSystem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(AbstractBoatEntity.class)
public abstract class AbstractBoatEntityMixin extends VehicleEntity {
    public AbstractBoatEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(method = "getWaterHeightBelow",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean grovesGetWaterHeightBelow(boolean original, @Local FluidState fluidState) {
        if (original)
            return true;

        return FluidSystem.FLUIDS.values().stream()
                .filter(FluidSystem::canBoatsWork)
                .anyMatch(data -> fluidState.isIn(data.fluidTag()));
    }

    @ModifyExpressionValue(method = "checkBoatInWater",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean grovesCheckBoatInWater(boolean original, @Local FluidState fluidState) {
        if (original)
            return true;

        return FluidSystem.FLUIDS.values().stream()
                .filter(FluidSystem::canBoatsWork)
                .anyMatch(data -> fluidState.isIn(data.fluidTag()));
    }

    @ModifyExpressionValue(method = "getUnderWaterLocation",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean grovesGetUnderWaterLocation(boolean original, @Local FluidState fluidState) {
        if (original)
            return true;

        return FluidSystem.FLUIDS.values().stream()
                .filter(FluidSystem::canBoatsWork)
                .anyMatch(data -> fluidState.isIn(data.fluidTag()));
    }

    @ModifyExpressionValue(method = "fall",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean grovesFall(boolean original, double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
        if (original)
            return true;

        FluidState fluidState = getWorld().getFluidState(getBlockPos().down());
        return FluidSystem.FLUIDS.values().stream()
                .filter(FluidSystem::canBoatsWork)
                .noneMatch(data -> fluidState.isIn(data.fluidTag()));
    }

    @ModifyExpressionValue(method = "canAddPassenger",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/vehicle/AbstractBoatEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean grovesCanAddPassenger(boolean original) {
        if (original)
            return true;

        return FluidSystem.FLUIDS.values().stream()
                .filter(FluidSystem::canBoatsWork)
                .noneMatch(data -> isSubmergedIn(data.fluidTag()));
    }}
