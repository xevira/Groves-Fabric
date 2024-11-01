package github.xevira.groves.client.screen.widget;

import github.xevira.groves.Groves;
import github.xevira.groves.mixin.fluid.WitchEntityMixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

@Environment(EnvType.CLIENT)
public abstract class SmallButtonWidget extends PressableWidget {
    private static final Identifier SMALL_BUTTON_DISABLED_TEXTURE = Groves.id("button_small_disabled");
    private static final Identifier SMALL_BUTTON_SELECTED_TEXTURE = Groves.id("button_small_selected");
    private static final Identifier SMALL_BUTTON_HIGHLIGHTED_TEXTURE = Groves.id("button_small_highlight");
    private static final Identifier SMALL_BUTTON_TEXTURE = Groves.id("button_small");


    private boolean disabled;

    protected SmallButtonWidget(int x, int y) {
        super(x, y, 10, 10, ScreenTexts.EMPTY);
    }

    protected SmallButtonWidget(int x, int y, Text message) {
        super(x, y, 10, 10, message);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        Identifier identifier;
        if (!this.active) {
            identifier = SMALL_BUTTON_DISABLED_TEXTURE;
        } else if (this.disabled) {
            identifier = SMALL_BUTTON_SELECTED_TEXTURE;
        } else if (this.isSelected()) {
            identifier = SMALL_BUTTON_HIGHLIGHTED_TEXTURE;
        } else {
            identifier = SMALL_BUTTON_TEXTURE;
        }

        context.drawGuiTexture(RenderLayer::getGuiTextured, identifier, this.getX(), this.getY(), this.width, this.height);
        this.renderExtra(context);
    }

    protected abstract void renderExtra(DrawContext context);

    public boolean isDisabled() {
        return this.disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }
}
