package github.xevira.groves.mixin.client;

import github.xevira.groves.Groves;
import github.xevira.groves.Registration;
import github.xevira.groves.poi.GrovesPOI;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("UnstableApiUsage")
@Environment(EnvType.CLIENT)
@Mixin(ChunkRendererRegion.class)
abstract class ChunkRendererRegionMixin {

    @Shadow
    @Final
    private World world;

    @Inject(method = "getColor(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/biome/ColorResolver;)I", at = @At("HEAD"), cancellable = true)
    private void getColor(BlockPos pos, ColorResolver colorResolver, CallbackInfoReturnable<Integer> clr)
    {
        ChunkPos chunkPos = new ChunkPos(pos);
        GrovesPOI.ClientGroveSanctuaryColorData colors = GrovesPOI.getChunkColors(this.world, chunkPos);

        if (colors != null) {
            if (colorResolver == BiomeColors.GRASS_COLOR) {
                if (colors.grass() >= 0) {
                    clr.setReturnValue(colors.grass());
                }
            } else if (colorResolver == BiomeColors.FOLIAGE_COLOR) {
                if (colors.foliage() >= 0) {
                    clr.setReturnValue(colors.foliage());
                }
            } else if (colorResolver == BiomeColors.WATER_COLOR) {
                if (colors.water() >= 0) {
                    clr.setReturnValue(colors.water());
                }
            }
        }
    }
}
