package github.xevira.groves.mixin;

import github.xevira.groves.Groves;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldView.class)
interface WorldViewMixin {

    @Inject(method = "getBiome(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/registry/entry/RegistryEntry;", at = @At("HEAD"), cancellable = true)
    private void getBiome(BlockPos pos, CallbackInfoReturnable<RegistryEntry<Biome>> clr)
    {
        WorldView wv = ((WorldView)(Object)this);
        if (wv instanceof World world) {
            if (world.isClient)
            {
                // Client things
            }
            else
            {
                // Server things
            }

        }
    }
}
