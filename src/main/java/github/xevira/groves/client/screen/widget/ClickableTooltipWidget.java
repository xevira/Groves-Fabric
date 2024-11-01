package github.xevira.groves.client.screen.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public abstract class ClickableTooltipWidget extends ClickableWidget {
    public ClickableTooltipWidget(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }


    protected boolean isPointInBounds(int x, int y, int width, int height, double pointX, double pointY)
    {
        pointX -= this.getX();
        pointY -= this.getY();

        return pointX >= (double)(x - 1) && pointX < (double)(x + width + 1) && pointY >= (double)(y - 1) && pointY < (double)(y + height + 1);
    }

    protected <T extends ClickableWidget> boolean isPointInControl(T widget, double pointX, double pointY)
    {
        return pointX >= (double)(widget.getX() - 1) && pointX < (double)(widget.getRight() + 1) && pointY >= (double)(widget.getY() - 1) && pointY < (double)(widget.getBottom() + 1);
    }

    public abstract void renderTooltips(DrawContext context, int mouseX, int mouseY, float delta);
}
