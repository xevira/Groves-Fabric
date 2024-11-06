package github.xevira.groves.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import github.xevira.groves.Groves;
import github.xevira.groves.poi.GrovesPOI;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.math.*;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ClientWorld.class)
abstract class ClientWorldMixin extends World {

    protected ClientWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    // I do not like this... as it is not compatible with other mods
    @ModifyExpressionValue(method = "getSkyColor(Lnet/minecraft/util/math/Vec3d;F)I",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/util/math/ColorHelper;getArgb(Lnet/minecraft/util/math/Vec3d;)I"))
    private int getSkyColorMixin(int original, Vec3d cameraPos, float tickDelta)
    {
//        Groves.LOGGER.info("getSkyColor(): camera = {}", cameraPos);

        float f = this.getSkyAngle(tickDelta);
        Vec3d vec3d = cameraPos.subtract(2.0, 2.0, 2.0).multiply(0.25);
        Vec3d vec3d2 = CubicSampler.sampleColor(vec3d, (x, y, z) -> {

            BlockPos pos = new BlockPos(x * 4, y * 4, z * 4);
            ChunkPos chunkPos = new ChunkPos(pos);
            GrovesPOI.ClientGroveSanctuaryColorData colors = GrovesPOI.getChunkColors(this, chunkPos);

            if (colors != null && colors.sky() >= 0) {
                return Vec3d.unpackRgb(colors.sky());
            }

            return Vec3d.unpackRgb(this.getBiomeAccess().getBiomeForNoiseGen(x, y, z).value().getSkyColor());
        });
        float g = MathHelper.cos(f * (float) (Math.PI * 2)) * 2.0F + 0.5F;
        g = MathHelper.clamp(g, 0.0F, 1.0F);
        vec3d2 = vec3d2.multiply((double)g);

        return ColorHelper.getArgb(vec3d2);
    }
}
