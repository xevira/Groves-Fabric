package github.xevira.groves.mixin.client;

import github.xevira.groves.util.ISlotVisibility;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {

    @Inject(method = "drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;)V", at = @At("Head"), cancellable = true)
    private void drawSlotVisibility(DrawContext context, Slot slot, CallbackInfo cb)
    {
        // Hide the slot if it's invisible
        if (slot instanceof ISlotVisibility vis)
        {
            if (!vis.isVisible())
            {
                cb.cancel();
            }
        }
    }
}
