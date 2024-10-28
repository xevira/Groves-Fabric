package github.xevira.groves.client.screen;

import github.xevira.groves.Groves;
import github.xevira.groves.screenhandler.GrovesSanctuaryScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrovesSanctuaryScreen extends HandledScreen<GrovesSanctuaryScreenHandler> {
    public static final Identifier BACKGROUND = Groves.id("textures/gui/container/groves_sanctuary_screen.png");

    public static final Text SUNLIGHT_TEXT = Groves.text("label", "groves.sunlight");
    public static final Text FOLIAGE_TEXT = Groves.text("label", "groves.foliage");

    private int sunlightLabelW;

    private final int tabBottomY;

    private ScreenTab currentTab = ScreenTab.GENERAL;

    private final Map<ScreenTab, List<Drawable>> tabControls = new HashMap<>();

    public GrovesSanctuaryScreen(GrovesSanctuaryScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.tabBottomY = 13;

        this.backgroundHeight = 179;
    }

    @Override
    protected void init() {
        super.init();

        this.sunlightLabelW = this.textRenderer.getWidth(SUNLIGHT_TEXT.asOrderedText());
    }

    private <T extends Drawable> T addTabDrawable(ScreenTab tab, T widget)
    {
        if (tabControls.containsKey(tab)) {
            List<Drawable> controls = tabControls.get(tab);

            controls.add(widget);
        }
        else
            tabControls.put(tab, List.of(widget));

        return this.addDrawable(widget);
    }

    private void initGeneralTab()
    {

    }

    private void setTabVisibility(ScreenTab tab, boolean visible) {
        if (tabControls.containsKey(tab)) {
            for(Drawable drawable : tabControls.get(tab))
            {
                if(drawable instanceof ClickableWidget widget)
                {
                    widget.visible = visible;
                }
            }
        }
    }

    private void selectTab(ScreenTab tab)
    {
        if (this.currentTab != tab)
        {
            setTabVisibility(this.currentTab, false);
            this.currentTab = tab;
            setTabVisibility(this.currentTab, true);
        }
    }

    private void drawSunlightProgressBar(DrawContext context, int x, int y)
    {
        long sunlight = this.handler.getSunlight();
        long maxSunlight = this.handler.getMaxSunlight();
        int percent = (int)(100 * sunlight / maxSunlight);

        context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, x, y, 0, this.backgroundHeight, 102, 10, 256, 256);
        context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, x + 1, y + 1, 0, this.backgroundHeight + 10, percent, 10, 256, 256);
    }

    private void drawGeneralForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, SUNLIGHT_TEXT, 4, 5, 0x404040, false);
        drawSunlightProgressBar(context, 6 + sunlightLabelW, 4);

        context.drawText(this.textRenderer, FOLIAGE_TEXT, 4, 20, 0x404040, false);
        int foliageLabelW = this.textRenderer.getWidth(FOLIAGE_TEXT.asOrderedText());
        int foliage = this.handler.getFoliage();
        Text foliageText = Groves.text("text", "groves.foliage", foliage, (foliage == 1) ? "" : "s");
        context.drawText(this.textRenderer, foliageText, foliageLabelW + 10, 20, 0x404040, false);
    }

    private void drawChunksForeground(DrawContext context, int mouseX, int mouseY) {
        // Draw Origin Chunk Pos

        // Iterator over the additional chunks

    }

    private void drawFriendsForeground(DrawContext context, int mouseX, int mouseY) {
    }

    private void drawAbilitiesForeground(DrawContext context, int mouseX, int mouseY) {
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.getMatrices().push();
        context.getMatrices().translate(0, this.tabBottomY, 0);

        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 0x404040, false);

        context.drawText(this.textRenderer, Groves.text("tooltip", "groves.tab." + this.currentTab.getName()), this.titleX, this.titleY + this.textRenderer.fontHeight + 2, 0x404040, false);

        context.getMatrices().translate(9, titleY + 2 * (this.textRenderer.fontHeight + 2) + 1, 0);

        switch(this.currentTab)
        {
            case GENERAL -> drawGeneralForeground(context, mouseX, mouseY);
            case CHUNKS -> drawChunksForeground(context, mouseX, mouseY);
            case FRIENDS -> drawFriendsForeground(context, mouseX, mouseY);
            case ABILITIES -> drawAbilitiesForeground(context, mouseX, mouseY);
        }

        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for(ScreenTab tab : ScreenTab.values())
        {
            if (tab.isPointInBounds(this.x, this.y, mouseX, mouseY))
            {
                this.currentTab = tab;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight, 256, 256);

        for(ScreenTab tab : ScreenTab.values())
        {
            tab.drawTab(context, this.x, this.y, currentTab == tab);
        }

        // TODO: Any tab backgrounds?
    }

    private void renderGeneralTooltips(DrawContext context, int shiftX, int shiftY, int mouseX, int mouseY, float delta) {
        if (isPointWithinBounds(shiftX + 7 + sunlightLabelW, shiftY + 5, 100, 8, mouseX, mouseY)) {
            long sunlight = this.handler.getSunlight();
            long maxSunlight = this.handler.getMaxSunlight();
            int percent = (int)(100 * sunlight / maxSunlight);
            Text sunlightLabel = Groves.text("tooltip", "groves.sunlight", sunlight, percent);
            context.drawTooltip(this.textRenderer, sunlightLabel, mouseX, mouseY);
        }
    }

    private void renderChunksTooltips(DrawContext context, int shiftX, int shiftY, int mouseX, int mouseY, float delta) {

    }

    private void renderFriendsTooltips(DrawContext context, int shiftX, int shiftY, int mouseX, int mouseY, float delta) {

    }

    private void renderAbilitiesTooltips(DrawContext context, int shiftX, int shiftY, int mouseX, int mouseY, float delta) {

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);

        // Tab tooltips
        for (ScreenTab tab : ScreenTab.values()) {
            if (tab.isPointInBounds(this.x, this.y, mouseX, mouseY)) {
                Text tabLabel = Groves.text("tooltip", "groves.tab." + tab.getName());
                context.drawTooltip(this.textRenderer, tabLabel, mouseX, mouseY);
                return;
            }
        }

        // Adjust to the tab control area
        int shiftX = 9;
        int shiftY = this.tabBottomY + this.titleY + 2 * (this.textRenderer.fontHeight + 2) + 1;

        // Selected Tab Contents
        switch(this.currentTab)
        {
            case GENERAL -> renderGeneralTooltips(context, shiftX, shiftY, mouseX, mouseY, delta);
            case CHUNKS -> renderChunksTooltips(context, shiftX, shiftY, mouseX, mouseY, delta);
            case FRIENDS -> renderFriendsTooltips(context, shiftX, shiftY, mouseX, mouseY, delta);
            case ABILITIES -> renderAbilitiesTooltips(context, shiftX, shiftY, mouseX, mouseY, delta);
        }
    }


    enum ScreenTab implements StringIdentifiable {
        GENERAL("general", 0, 3),
        CHUNKS("chunks", 1, 29),
        FRIENDS("friends", 2, 55),
        ABILITIES("abilities", 3, 81);

        private final String name;

        private final int index;
        private final int x;

        ScreenTab(String name, int index, int x) {
            this.name = name;

            this.index = index;
            this.x = x;
        }

        public int getIndex()
        {
            return this.index;
        }

        public int getX()
        {
            return this.x;
        }

        public String getName()
        {
            return this.name;
        }

        public void drawTab(DrawContext context, int left, int top, boolean selected)
        {
            // Tab background
            context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, left + x, top, 176, selected ? 0 : 16, 26, 16, 256, 256);

            // Tab icon
            context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, left + x + 9, top + 4, 176 + 8 * this.index, selected ? 32 : 40, 8, 8, 256, 256);
        }

        public boolean isPointInBounds(int left, int top, double mouseX, double mouseY)
        {
            mouseX -= (double)left;
            mouseY -= (double)top;

            return mouseX >= (double)(x - 1) && mouseX < (double)(x + 26 + 1) && mouseY >= (double)(-1) && mouseY < (double)(13 + 1);
        }

        @Override
        public String asString() {
            return this.name;
        }
    }
}
