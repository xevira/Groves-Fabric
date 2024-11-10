package github.xevira.groves.client.screen.widget;

import github.xevira.groves.Groves;
import github.xevira.groves.util.ScreenTab;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public abstract class TabControlWidget extends ClickableWidget {

    protected static final int TAB_WIDTH = 162;
    protected static final int TAB_HEIGHT = 131;

    protected final ScreenTab tab;
    protected final Identifier texture;
    protected final MutableText _label;
    protected final MutableText _tooltip;

    private final List<ClickableWidget> children = new ArrayList<>();
    protected Screen screen;

    public TabControlWidget(ScreenTab tab, int x, int y) {
        super(x, y, TAB_WIDTH, TAB_HEIGHT, Text.empty());

        this.tab = tab;
        this.texture = Groves.id("textures/gui/container/sanctuary/tab_" + tab.getName() + ".png");
        this._label = Groves.text("label", "groves.tab." + tab.getName());
        this._tooltip = Groves.text("tooltip", "groves.tab." + tab.getName());
    }

    public void setScreen(Screen screen) { this.screen = screen; }

    public ScreenTab getTab() { return this.tab; }

    public MutableText getLabelText() { return this._label; }

    public MutableText getTooltipText() { return this._tooltip; }

    public void addChildElement(ClickableWidget element)
    {
        this.children.add(element);
    }

    public List<ClickableWidget> getChildren()
    {
        return this.children;
    }

    public void setVisible(boolean visible)
    {
        this.visible = visible;
        this.children.forEach(element -> element.visible = visible);
    }

    public void tick()
    {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Groves.LOGGER.info("mouseClicked: {}, {}, {}", mouseX, mouseY, button);
        for(ClickableWidget widget : getChildren())
        {
            if (isPointInControl(widget, mouseX, mouseY))
            {
                widget.mouseClicked(mouseX, mouseY, button);
                this.screen.setFocused(widget);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawTexture(RenderLayer::getGuiTextured, this.texture, this.getX(), this.getY(), 0, 0, TAB_WIDTH, TAB_HEIGHT, 256, 256);
    }

    public boolean isPointInBounds(int x, int y, int width, int height, double pointX, double pointY)
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
