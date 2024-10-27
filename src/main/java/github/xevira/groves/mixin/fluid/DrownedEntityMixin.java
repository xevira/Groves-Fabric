package github.xevira.groves.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import github.xevira.groves.fluid.FluidSystem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DrownedEntity.class)
public class DrownedEntityMixin {
    @ModifyExpressionValue(method = "canSpawn(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z",
                    ordinal = 0))
    private static boolean grovesCanSpawn1(boolean original, EntityType<?> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        FluidState fluidState = world.getFluidState(pos.down());

        boolean result = original;
        for (FluidSystem fluidSystem : FluidSystem.FLUIDS.values()) {
            if(fluidSystem.canDrownedSpawn())
                continue;

            result = original || !fluidState.isIn(fluidSystem.fluidTag());
        }

        return result;
    }

    @ModifyExpressionValue(method = "canSpawn(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z",
                    ordinal = 1))
    private static boolean grovesCanSpawn2(boolean original, EntityType<?> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        FluidState fluidState = world.getFluidState(pos.down());

        return original || FluidSystem.FLUIDS.values().stream()
                .filter(FluidSystem::canDrownedSpawn)
                .anyMatch(fluidData -> fluidState.isIn(fluidData.fluidTag()));
    }
}