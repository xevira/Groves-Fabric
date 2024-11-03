package github.xevira.groves.mixin.client;

import github.xevira.groves.ClientConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    @Inject(method = "shouldFilterText()Z", at = @At("HEAD"), cancellable = true)
    private void shouldFilterTextMixin(CallbackInfoReturnable<Boolean> cb)
    {
        if (ClientConfig.DEBUG_FILTER_PROFANITY)
            cb.setReturnValue(true);
    }

}
