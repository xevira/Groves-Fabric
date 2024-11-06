package github.xevira.groves.mixin.client;

import github.xevira.groves.Groves;
import github.xevira.groves.Registration;
import github.xevira.groves.poi.GrovesPOI;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("UnstableApiUsage")
@Environment(EnvType.CLIENT)
@Mixin(BiomeColors.class)
public abstract class BiomeColorsMixin {

    @Inject(method = "getGrassColor(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/util/math/BlockPos;)I", at = @At("HEAD"), cancellable = true)
    private static void getGrassColorMixin(BlockRenderView world, BlockPos pos, CallbackInfoReturnable<Integer> clr)
    {
        if (world instanceof ClientWorld clientWorld)
        {
            Chunk chunk = clientWorld.getChunk(pos);

            if (chunk != null)
            {
                GrovesPOI.ClientGroveSanctuaryColorData colors = chunk.getAttached(Registration.SANCTUARY_COLOR_DATA);

                if (colors != null && colors.grass() >= 0)
                {
                    Groves.LOGGER.info("getGrassColorMixin({}): {}", pos, colors.grass());
                    clr.setReturnValue(colors.grass());
                }
            }
        }
    }
}
