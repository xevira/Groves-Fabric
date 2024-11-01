package github.xevira.groves.client.screen.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ToggleButtonWidget extends SmallButtonWidget {
    private final Text tooltipOn;
    private final Text tooltipOff;

    private final PressAction onPress;

    public ToggleButtonWidget(int x, int y, Text message, Text on, Text off, PressAction onPress) {
        super(x, y, message);

        this.tooltipOn = on.copy();
        this.tooltipOff = off.copy();

        this.onPress = onPress;
    }

    @Override
    protected void renderExtra(DrawContext context) {

    }

    @Override
    public void setDisabled(boolean disabled) {
        super.setDisabled(disabled);
        this.setTooltip(Tooltip.of(disabled ? this.tooltipOn : this.tooltipOff));
    }

    @Override
    public void onPress() {
        // Toggle
        this.setDisabled(!this.isDisabled());
        this.onPress.onPress(this);
    }

    @FunctionalInterface
    public interface PressAction {
        void onPress(ToggleButtonWidget button);
    }
}
