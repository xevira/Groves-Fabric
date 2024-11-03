package github.xevira.groves.mixin.client;

import github.xevira.groves.Groves;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(TextFieldWidget.class)
public abstract class TextFieldWidgetMixin extends ClickableWidget {

    public TextFieldWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @Shadow
    abstract boolean isEditable();

    @Inject(method = "keyPressed(III)Z", at=@At("HEAD"), cancellable = true)
    private void keyPressed(int keycode, int scancode, int modifiers, CallbackInfoReturnable<Boolean> clr)
    {
//        Groves.LOGGER.info("keyPressed({}, {}, {})", keycode, scancode, modifiers);
    }

    @Inject(method = "isActive()Z", at = @At("HEAD"), cancellable = true)
    private void isActive(CallbackInfoReturnable<Boolean> clr)
    {
//        Groves.LOGGER.info("isActive(): N = {}, F = {}, E = {}", isNarratable(), isFocused(), isEditable());
    }
}
