package github.xevira.groves.client.screen.widget;

import github.xevira.groves.Groves;
import github.xevira.groves.poi.GrovesPOI;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.world.BlockLocating;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListControlWidget<T extends ClickableTooltipWidget> extends ClickableWidget {
    private final List<T> entries = new ArrayList<>();

    private int yOffset;
    private int dataHeight;

    public ListControlWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Text.empty());

    }

    public int size()
    {
        return this.entries.size();
    }

    public int getDataHeight()
    {
        return this.dataHeight;
    }

    public boolean needsScrollbar()
    {
        return this.dataHeight > this.getHeight();
    }

    public List<T> getEntries()
    {
        return this.entries;
    }

    private void updateEntries()
    {
        int y = this.getY() - this.yOffset;

        // Update the entry positions
        for(T entry : entries)
        {
            entry.setPosition(entry.getX(), y);

            y += entry.getHeight();
        }
    }

    public void setYOffset(int yOffset)
    {
        this.yOffset = yOffset;
        updateEntries();
    }

    private void recalculateDataHeight()
    {
        this.dataHeight = 0;
        for(T entry : entries)
        {
            this.dataHeight += entry.getHeight();
        }
    }

    public void clear()
    {
        this.entries.clear();
        this.dataHeight = 0;
    }

    public void addEntry(T entry)
    {
        this.entries.add(entry);
        updateEntries();
        recalculateDataHeight();
    }

    public void addEntries(Collection<T> entries)
    {
        this.entries.addAll(entries);
        updateEntries();
        recalculateDataHeight();
    }

    public void removeEntry(T entry)
    {
        if (this.entries.remove(entry)) {
            updateEntries();
            recalculateDataHeight();
        }
    }

    public void removeEntry(int index)
    {
        this.entries.remove(index);
        updateEntries();
        recalculateDataHeight();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for(T entry : entries)
        {
            if (isPointInControl(entry, mouseX, mouseY)) {
                if (entry.mouseClicked(mouseX, mouseY, button))
                    return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.enableScissor(getX(), getY(), getRight(), getBottom());

        for(T entry : entries)
        {
            // Only render entries that would be visible
            if (entry.getBottom() >= getY() && entry.getY() <= getBottom())
            {
                entry.render(context, mouseX, mouseY, delta);
            }
        }

        context.disableScissor();
   }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    private boolean isPointInBounds(int x, int y, int width, int height, double pointX, double pointY)
    {
        pointX -= this.getX();
        pointY -= this.getY();

        return pointX >= (double)(x - 1) && pointX < (double)(x + width + 1) && pointY >= (double)(y - 1) && pointY < (double)(y + height + 1);
    }

    private <T extends ClickableWidget> boolean isPointInControl(T widget, double pointX, double pointY)
    {
        return pointX >= (double)(widget.getX() - 1) && pointX < (double)(widget.getRight() + 1) && pointY >= (double)(widget.getY() - 1) && pointY < (double)(widget.getBottom() + 1);
    }

    public void renderTooltips(DrawContext context, int mouseX, int mouseY, float delta)
    {
        for(T entry : this.entries)
        {
            if (isPointInControl(entry, mouseX, mouseY))
            {
                entry.renderTooltips(context, mouseX, mouseY, delta);
                return;
            }
        }

    }

}
