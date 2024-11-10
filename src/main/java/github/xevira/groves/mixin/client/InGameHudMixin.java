package github.xevira.groves.mixin.client;

import github.xevira.groves.events.client.HudRenderEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void tickMixin(CallbackInfo cb)
    {
        HudRenderEvents.handleTick();
    }
}
