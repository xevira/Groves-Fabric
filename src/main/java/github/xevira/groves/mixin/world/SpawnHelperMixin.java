package github.xevira.groves.mixin.world;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import github.xevira.groves.poi.WindChimes;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SpawnHelper.class)
public abstract class SpawnHelperMixin {

    @ModifyExpressionValue(method = "canSpawn",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/SpawnRestriction;canSpawn(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)Z"))
    private static boolean canSpawn(boolean original,
            ServerWorld world,
            SpawnGroup group,
            StructureAccessor structureAccessor,
            ChunkGenerator chunkGenerator,
            SpawnSettings.SpawnEntry spawnEntry,
            BlockPos.Mutable pos,
            double squaredDistance)
    {
        if (original && group == SpawnGroup.MONSTER)
            return WindChimes.canSpawn(world, pos);

        return original;
    }
}
