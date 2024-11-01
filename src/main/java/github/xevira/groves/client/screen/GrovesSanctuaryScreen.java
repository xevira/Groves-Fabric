package github.xevira.groves.client.screen;

import github.xevira.groves.Groves;
import github.xevira.groves.client.screen.widget.*;
import github.xevira.groves.network.SetChunkLoadingPayload;
import github.xevira.groves.poi.GrovesPOI;
import github.xevira.groves.screenhandler.GrovesSanctuaryScreenHandler;
import github.xevira.groves.util.ScreenTab;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;

import java.util.*;

public class GrovesSanctuaryScreen extends HandledScreen<GrovesSanctuaryScreenHandler> {
    public static final Identifier BACKGROUND = Groves.id("textures/gui/container/sanctuary/background.png");

    public static final Identifier SCROLLBAR = Groves.id("scrollbar");
    public static final Identifier SCROLLBAR_DISABLED = Groves.id("scrollbar_disabled");

    private final int tabBottomY;

    private final int tabBackgroundWidth;
    private final int tabBackgroundHeight;

//    private final Map<ScreenTab, List<Drawable>> tabControls = new HashMap<>();

    private final Map<ScreenTab, TabControlWidget> Tabs = new HashMap<>();
    private TabControlWidget currentTab;

    public GrovesSanctuaryScreen(GrovesSanctuaryScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.tabBottomY = 13;

        this.backgroundHeight = 179;

        this.tabBackgroundWidth = 162;
        this.tabBackgroundHeight = 131;

        List<GrovesPOI.ClientGroveSanctuary.ChunkData> chunks = this.handler.getSanctuary().getChunks();
        for(GrovesPOI.ClientGroveSanctuary.ChunkData chunk : chunks)
        {
            Groves.LOGGER.info("Screen: chunk = {} ({})", chunk.pos(), chunk.chunkLoad());
        }
    }

    @Override
    protected void init() {
        super.init();

        Tabs.put(ScreenTab.GENERAL, new TabGeneralWidget(this.x + 7, this.y + 41, this.handler));
        Tabs.put(ScreenTab.CHUNKS, new TabChunksWidget(this.x + 7, this.y + 41, this.handler).addChunks(this.handler.getChunks()));
        Tabs.put(ScreenTab.FRIENDS, new TabFriendsWidget(this.x + 7, this.y + 41, this.handler));
        Tabs.put(ScreenTab.ABILITIES, new TabAbilitiesWidget(this.x + 7, this.y + 41, this.handler));
        Tabs.put(ScreenTab.KEYBINDS, new TabKeybindsWidget(this.x + 7, this.y + 41, this.handler));

        for(TabControlWidget widget : Tabs.values())
        {
            this.addDrawableChild(widget);
            widget.visible = false;
        }

        handler.setCurrentTab(ScreenTab.GENERAL);
        this.currentTab = Tabs.get(ScreenTab.GENERAL);
        this.currentTab.visible = true;
    }

    private void selectTab(TabControlWidget tab) {
        if (!this.handler.isSelectedTab(tab.getTab())) {
            this.handler.setCurrentTab(tab.getTab());
            tab.visible = true;
            this.currentTab.visible = false;
            this.currentTab = tab;
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.getMatrices().push();
        context.getMatrices().translate(0, this.tabBottomY, 0);

        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 0x404040, false);
        context.drawText(this.textRenderer, this.currentTab.getLabelText(), this.titleX, this.titleY + this.textRenderer.fontHeight + 2, 0x404040, false);

        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for(TabControlWidget tab : Tabs.values()) {
            if (isPointWithinBounds(tab.getTab().ordinal() * 26 + 3, 0, 26, 13, mouseX, mouseY))
            {
                selectTab(tab);
                return true;
            }
        }

  //      if (this.currentTab.mouseClicked(mouseX, mouseY, button)) return true;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
//        if (this.currentTab.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (this.currentTab.getTab() == ScreenTab.CHUNKS) {
            if (isPointWithinBounds(this.currentTab.getX() - this.x, this.currentTab.getY() - this.y, this.currentTab.getWidth(), this.currentTab.getHeight(), mouseX, mouseY))
            {
                this.currentTab.mouseMoved(mouseX, mouseY);
            }
        }
    }

    public void drawTab(DrawContext context, TabControlWidget tab, boolean selected) {
        int index = tab.getTab().ordinal();
        int x = index * 26 + 3;

        // Tab background
        context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, this.x + x, this.y, this.backgroundWidth, selected ? 0 : 16, 26, 16, 256, 256);

        // Tab icon
        context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, this.x + x + 4, this.y + 4, this.backgroundWidth + (selected ? 0 : 18), 32 + 8 * index, 18, 8, 256, 256);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight, 256, 256);

        // Render tab .. tabs
        for(TabControlWidget tab : Tabs.values())
        {
            drawTab(context, tab, this.currentTab == tab);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Drawables
        super.render(context, mouseX, mouseY, delta);

        // Tooltips
        drawMouseoverTooltip(context, mouseX, mouseY);

        this.currentTab.renderTooltips(context, mouseX, mouseY, delta);

        int errorTicks = this.handler.getBuyChunkTicks();
        if (errorTicks > 0)
        {
            int color = ((255 * errorTicks / 100) << 24) | 0xFF0000;

            context.drawCenteredTextWithShadow(this.textRenderer, this.handler.getBuyChunkReason(), this.width / 2, this.height / 2, color);
        }
    }

    static abstract class TabControl {
        protected static final int TAB_WIDTH = 162;
        protected static final int TAB_HEIGHT = 131;

        protected final ScreenTab tab;
        protected final Identifier texture;
        protected final int x;
        protected final int y;
        protected final int wx;
        protected final int wy;
        protected final TextRenderer textRenderer;
        protected final GrovesSanctuaryScreenHandler handler;
        protected final MutableText label;
        protected final MutableText tooltip;

        protected final List<Element> children = new ArrayList<>();
        protected final List<Selectable> selectables = new ArrayList<>();
        protected final List<Drawable> drawables = new ArrayList<>();

        TabControl(final ScreenTab tab, final GrovesSanctuaryScreenHandler handler, final TextRenderer textRenderer, final int wx, final int wy, final int x, final int y)
        {
            this.tab = tab;
            this.texture = Groves.id("textures/gui/container/sanctuary/tab_" + tab.getName() + ".png");
            this.label = Groves.text("label", "groves.tab." + tab.getName());
            this.tooltip = Groves.text("tooltip", "groves.tab." + tab.getName());
            this.textRenderer = textRenderer;
            this.handler = handler;
            this.wx = wx;
            this.wy = wy;
            this.x = x;
            this.y = y;
        }

        public ScreenTab getTab() { return this.tab; }

        public MutableText getLabel() { return this.label; }

        public MutableText getTooltip() { return this.tooltip; }

        public int getX() { return this.x; }
        public int getY() { return this.y; }

        public void drawTab(DrawContext context, int left, int top, int u, int v, boolean selected) {
            int index = tab.ordinal();
            int x = index * 26 + 3;

            // Tab background
            context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, left + x, top, 176, selected ? 0 : 16, 26, 16, 256, 256);

            // Tab icon
            context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, left + x + 4, top + 4, u + (selected ? 0 : 18), v + 8 * index, 18, 8, 256, 256);
        }

        // Is the mouse over one of the tabs at the top of the screen
        public boolean isMouseOverTab(int left, int top, double mouseX, double mouseY) {
            mouseX -= (double) left;
            mouseY -= (double) top;
            int x = tab.ordinal() * 26 + 3;

            return mouseX >= (double) (x - 1) && mouseX < (double) (x + 26 + 1) && mouseY >= (double) (-1) && mouseY < (double) (13 + 1);
        }

        public boolean isMouseInsideTabElement(int x, int y, int width, int height, double pointX, double pointY)
        {
            pointX -= (double)(this.wx + this.x);
            pointY -= (double)(this.wy + this.y);
            return pointX >= (double)(x - 1) && pointX < (double)(x + width + 1) && pointY >= (double)(y - 1) && pointY < (double)(y + height + 1);
        }

        public abstract void drawForeground(DrawContext context, int mouseX, int mouseY);

        public void drawBackground(DrawContext context, float delta, int mouseX, int mouseY)
        {
            context.drawTexture(RenderLayer::getGuiTextured, texture, this.wx + this.x, this.wy + this.y, 0, 0, TAB_WIDTH, TAB_HEIGHT, 256, 256);
        }

        public abstract boolean mouseClicked(double mouseX, double mouseY, int button);

        public abstract boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY);

        public abstract boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount);

        public boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button)  { return false; }

        public abstract  void renderTooltips(DrawContext context, int mouseX, int mouseY, float delta);

        public final boolean renderTabTooltip(DrawContext context, int x, int y, int mouseX, int mouseY, float delta)
        {
            if (isMouseOverTab(x, y, mouseX, mouseY))
            {
                context.drawTooltip(this.textRenderer, this.tooltip, mouseX, mouseY);
                return true;
            }

            return false;
        }

        protected <T extends Element & Drawable & Selectable> T addDrawableChild(T drawableElement) {
            this.drawables.add(drawableElement);
            return this.addSelectableChild(drawableElement);
        }

        protected <T extends Drawable> T addDrawable(T drawable) {
            this.drawables.add(drawable);
            return drawable;
        }

        protected <T extends Element & Selectable> T addSelectableChild(T child) {
            this.children.add(child);
            this.selectables.add(child);
            return child;
        }

        public void render(DrawContext context, int mouseX, int mouseY, float delta)
        {
            for(Drawable drawable : this.drawables)
            {
                drawable.render(context, mouseX, mouseY, delta);
            }
        }
    }

    static class TabGeneral extends TabControl {
        public static final Text SUNLIGHT_TEXT = Groves.text("label", "groves.sunlight");
        public static final Text FOLIAGE_TEXT = Groves.text("label", "groves.foliage");
        public static final Text MOONWELL_TEXT = Groves.text("label", "groves.moonwell");
        public static final Text NO_MOONWELL_TEXT = Groves.text("text", "groves.no_moonwell");

        private final int sunlightLabelW;
        private final int foliageLabelW;
        private final int moonwellLabelW;

        TabGeneral(GrovesSanctuaryScreenHandler handler, TextRenderer textRenderer, int wx, int wy, int x, int y) {
            super(ScreenTab.GENERAL, handler, textRenderer, wx, wy, x, y);

            // Create control elements
            this.sunlightLabelW = this.textRenderer.getWidth(SUNLIGHT_TEXT.asOrderedText());
            this.foliageLabelW = this.textRenderer.getWidth(FOLIAGE_TEXT.asOrderedText());
            this.moonwellLabelW = this.textRenderer.getWidth(MOONWELL_TEXT.asOrderedText());
        }

        @Override
        public void drawForeground(DrawContext context, int mouseX, int mouseY) {
            drawSunlightProgressBar(context, 5, 5);
            drawFoliage(context, 5, 21);
            drawMoonwell(context, 5, 37);
        }

        @Override
        public void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
            super.drawBackground(context, delta, mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            return false;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            return false;
        }

        @Override
        public boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
            return false;
        }

        @Override
        public void renderTooltips(DrawContext context, int mouseX, int mouseY, float delta) {
            if (isMouseInsideTabElement(sunlightLabelW + 1, 5, 100, 8, mouseX, mouseY)) {
                long sunlight = this.handler.getSunlight();
                long maxSunlight = this.handler.getMaxSunlight();
                int percent = (int) (100 * sunlight / maxSunlight);
                Text sunlightLabel = Groves.text("tooltip", "groves.sunlight", sunlight, percent);
                context.drawTooltip(this.textRenderer, sunlightLabel, mouseX, mouseY);
                return;
            }
        }

        private void drawSunlightProgressBar(DrawContext context, int x, int y) {
            long sunlight = this.handler.getSunlight();
            long maxSunlight = this.handler.getMaxSunlight();
            int percent = (int) (100 * sunlight / maxSunlight);

            context.drawText(this.textRenderer, SUNLIGHT_TEXT, x, y + 1, 0x404040, false);

            // Background of the bar
            context.drawTexture(RenderLayer::getGuiTextured, texture, x + sunlightLabelW + 2, y, 0, TAB_HEIGHT, 102, 10, 256, 256);

            // Foreground of the bar
            context.drawTexture(RenderLayer::getGuiTextured, texture, x + sunlightLabelW + 3, y + 1, 0, TAB_HEIGHT + 10, percent, 10, 256, 256);
        }

        private void drawFoliage(DrawContext context, int x, int y) {
            context.drawText(this.textRenderer, FOLIAGE_TEXT, x, y, 0x404040, false);
            int foliage = this.handler.getFoliage();
            Text foliageText = Groves.text("text", "groves.foliage", foliage, (foliage == 1) ? "" : "s");
            context.drawText(this.textRenderer, foliageText, x + foliageLabelW + 2, 20, 0x404040, false);
        }

        private void drawMoonwell(DrawContext context, int x, int y) {
            context.drawText(this.textRenderer, MOONWELL_TEXT, x, y, 0x404040, false);
            BlockPos pos = this.handler.getMoonwell();
            if (pos != null) {
                Text moonwellText = Groves.text("text", "groves.moonwell", pos.getX(), pos.getY(), pos.getZ());
                context.drawText(this.textRenderer, moonwellText, x + moonwellLabelW + 2, y, 0x404040, false);
            } else
                context.drawText(this.textRenderer, NO_MOONWELL_TEXT, x + moonwellLabelW + 2, y, 0x404040, false);
        }
    }

    static class TabChunks extends TabControl {
        private static final int ROW_HEIGHT = 12;

        // Scrollbar data
        private boolean hasTooManyChunks;
        private float scrollPosition;
        private boolean scrollbarClicked;
        private int scrollBarY;
        private int visibleTopRow;

        TabChunks(GrovesSanctuaryScreenHandler handler, TextRenderer textRenderer, int wx, int wy, int x, int y) {
            super(ScreenTab.CHUNKS, handler, textRenderer, wx, wy, x, y);


        }

        @Override
        public void drawForeground(DrawContext context, int mouseX, int mouseY) {
            context.enableScissor(this.wx + this.x + 1, this.wy + this.y + 13, this.wx + this.x + 145, this.wy + this.y + 109);

            List<GrovesPOI.ClientGroveSanctuary.ChunkData> chunks = this.handler.getChunks();
            int startIndex;
            int endIndex;
            int yOffset;

            int pixels = chunks.size() * ROW_HEIGHT;
            if (pixels <= 97)
            {
                startIndex = 0;
                endIndex = chunks.size() - 1;
                yOffset = 0;
            }
            else {
                int p = (int)((pixels - 97) * this.scrollPosition);
                startIndex = p / ROW_HEIGHT;
                endIndex = Math.min(chunks.size() - 1, startIndex + 5);
                yOffset = p % ROW_HEIGHT;
            }

            int y = 13 - yOffset;
            for(int i = startIndex; i <= endIndex; i++)
            {
                GrovesPOI.ClientGroveSanctuary.ChunkData chunk = chunks.get(i);

                // Do a button

                context.drawText(this.textRenderer, String.format("(%d, %d)", chunk.pos().x, chunk.pos().z), 3, y + 2, 0x404040, false);
                y += ROW_HEIGHT;
            }

            context.disableScissor();
        }

        @Override
        public void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
            super.drawBackground(context, delta, mouseX, mouseY);

            int scrollBarY = (int)(82.0F * this.scrollPosition);

            List<GrovesPOI.ClientGroveSanctuary.ChunkData> chunks = this.handler.getChunks();
            int pixels = chunks.size() * ROW_HEIGHT;
            Identifier bar = (pixels > 97) ? GrovesSanctuaryScreen.SCROLLBAR : GrovesSanctuaryScreen.SCROLLBAR_DISABLED;
            context.drawGuiTexture(RenderLayer::getGuiTextured, bar, this.wx + this.x + 149, this.wy + this.y + 13 + scrollBarY, 12, 15);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.scrollbarClicked = false;
            List<GrovesPOI.ClientGroveSanctuary.ChunkData> chunks = this.handler.getChunks();
            int pixels = chunks.size() * ROW_HEIGHT;

            if (pixels > 97 && isMouseInsideTabElement(149, 13, 12, 97, mouseX, mouseY))
            {
                this.scrollbarClicked = true;
            }

            return false;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (this.scrollbarClicked)
            {
                int j = this.wy + this.y + 13;
                int k = j + 97;
                this.scrollPosition = ((float)mouseY - (float)j - 7.5F) / (82.0F);
                this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0F, 1.0F);
                //this.visibleTopRow = Math.max((int)((double)(this.scrollPosition * (float)i) + 0.5), 0);

                return true;
            }

            return false;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            return false;
        }

        @Override
        public void renderTooltips(DrawContext context, int mouseX, int mouseY, float delta) {
        }
    }

    static class TabFriends extends TabControl {
        TabFriends(GrovesSanctuaryScreenHandler handler, TextRenderer textRenderer, int wx, int wy, int x, int y) {
            super(ScreenTab.FRIENDS, handler, textRenderer, wx, wy, x, y);
        }

        @Override
        public void drawForeground(DrawContext context, int mouseX, int mouseY) {

        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            return false;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            return false;
        }

        @Override
        public void renderTooltips(DrawContext context, int mouseX, int mouseY, float delta) {

        }
    }

    static class TabAbilities extends TabControl {
        TabAbilities(GrovesSanctuaryScreenHandler handler, TextRenderer textRenderer, int wx, int wy, int x, int y) {
            super(ScreenTab.ABILITIES, handler, textRenderer, wx, wy, x, y);
        }

        @Override
        public void drawForeground(DrawContext context, int mouseX, int mouseY) {

        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            return false;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            return false;
        }

        @Override
        public void renderTooltips(DrawContext context, int mouseX, int mouseY, float delta) {

        }
    }

    static class TabKeybinds extends TabControl {
        TabKeybinds(GrovesSanctuaryScreenHandler handler, TextRenderer textRenderer, int wx, int wy, int x, int y) {
            super(ScreenTab.KEYBINDS, handler, textRenderer, wx, wy, x, y);
        }

        @Override
        public void drawForeground(DrawContext context, int mouseX, int mouseY) {

        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            return false;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            return false;
        }

        @Override
        public void renderTooltips(DrawContext context, int mouseX, int mouseY, float delta) {

        }
    }

    static class TabGeneralWidget extends TabControlWidget {
        public static final Text SUNLIGHT_TEXT = Groves.text("label", "groves.sunlight");
        public static final Text FOLIAGE_TEXT = Groves.text("label", "groves.foliage");
        public static final Text MOONWELL_TEXT = Groves.text("label", "groves.moonwell");
        public static final Text NO_MOONWELL_TEXT = Groves.text("text", "groves.no_moonwell");

        private final int sunlightLabelW;
        private final int foliageLabelW;
        private final int moonwellLabelW;

        private final TextRenderer textRenderer;

        private final GrovesSanctuaryScreenHandler handler;

        public TabGeneralWidget(int x, int y, GrovesSanctuaryScreenHandler handler) {
            super(ScreenTab.GENERAL, x, y);

            this.textRenderer = MinecraftClient.getInstance().textRenderer;

            this.handler = handler;

            this.sunlightLabelW = this.textRenderer.getWidth(SUNLIGHT_TEXT.asOrderedText());
            this.foliageLabelW = this.textRenderer.getWidth(FOLIAGE_TEXT.asOrderedText());
            this.moonwellLabelW = this.textRenderer.getWidth(MOONWELL_TEXT.asOrderedText());
        }

        private void drawSunlightProgressBar(DrawContext context, int x, int y) {
            long sunlight = this.handler.getSunlight();
            long maxSunlight = this.handler.getMaxSunlight();
            int percent = (int) (100 * sunlight / maxSunlight);

            context.drawText(this.textRenderer, SUNLIGHT_TEXT, x, y + 1, 0x404040, false);

            // Background of the bar
            context.drawTexture(RenderLayer::getGuiTextured, texture, x + sunlightLabelW + 2, y, 0, TAB_HEIGHT, 102, 10, 256, 256);

            // Foreground of the bar
            context.drawTexture(RenderLayer::getGuiTextured, texture, x + sunlightLabelW + 3, y + 1, 0, TAB_HEIGHT + 10, percent, 10, 256, 256);
        }

        private void drawFoliage(DrawContext context, int x, int y) {
            context.drawText(this.textRenderer, FOLIAGE_TEXT, x, y, 0x404040, false);
            int foliage = this.handler.getFoliage();
            Text foliageText = Groves.text("text", "groves.foliage", foliage, (foliage == 1) ? "" : "s");
            context.drawText(this.textRenderer, foliageText, x + foliageLabelW + 2, y, 0x404040, false);
        }

        private void drawMoonwell(DrawContext context, int x, int y) {
            context.drawText(this.textRenderer, MOONWELL_TEXT, x, y, 0x404040, false);
            BlockPos pos = this.handler.getMoonwell();
            if (pos != null) {
                Text moonwellText = Groves.text("text", "groves.moonwell", pos.getX(), pos.getY(), pos.getZ());
                context.drawText(this.textRenderer, moonwellText, x + moonwellLabelW + 2, y, 0x404040, false);
            } else
                context.drawText(this.textRenderer, NO_MOONWELL_TEXT, x + moonwellLabelW + 2, y, 0x404040, false);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);

            drawSunlightProgressBar(context, this.getX() + 5, this.getY() + 5);
            drawFoliage(context, this.getX() + 5, this.getY() + 21);
            drawMoonwell(context, this.getX() + 5, this.getY() + 37);
        }

        @Override
        public void renderTooltips(DrawContext context, int mouseX, int mouseY, float delta) {
            if (isPointInBounds(sunlightLabelW + 1, 5, 100, 8, mouseX, mouseY)) {
                long sunlight = this.handler.getSunlight();
                long maxSunlight = this.handler.getMaxSunlight();
                int percent = (int) (100 * sunlight / maxSunlight);
                Text sunlightLabel = Groves.text("tooltip", "groves.sunlight", sunlight, percent);
                context.drawTooltip(this.textRenderer, sunlightLabel, mouseX, mouseY);
            }
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {

        }
    }

    static class TabChunksWidget extends TabControlWidget {
        private static int COLOR_OFF = 0x400000;
        private static int COLOR_ON = 0x004000;

        private static final Text TOOLTIP_ON = Groves.text("tooltip", "groves.chunks.keep_loaded.on");
        private static final Text TOOLTIP_OFF = Groves.text("tooltip", "groves.chunks.keep_loaded.off");

        private static final int ROW_HEIGHT = 12;

        private final GrovesSanctuaryScreenHandler handler;

//        private final ToggleButtonWidget originToggleWidget;
//        private final ChunkDataListControlWidget listControlWidget;
        private final ChunkGridWidget mapWidget;

        // Scrollbar data
        private boolean hasTooManyChunks;
        private int scrollBarY;
        private float scrollPosition;
        private boolean scrollbarClicked;
        private int visibleTopRow;

        public TabChunksWidget(int x, int y, GrovesSanctuaryScreenHandler handler) {
            super(ScreenTab.CHUNKS, x, y);
            this.handler = handler;

            mapWidget = new ChunkGridWidget(x + 1, y + 1, TAB_WIDTH - 2, TAB_HEIGHT - 2, this.handler.getChunkMap())
                    .setOrigin(this.handler.getOrigin());

//            GrovesPOI.ClientGroveSanctuary.ChunkData origin = this.handler.getOrigin();
//            this.originToggleWidget = new ToggleButtonWidget(x + 2, y + 1, Text.empty(),
//                    Groves.text("tooltip", "groves.chunks.keep_loaded.on", origin.pos().x, origin.pos().z),
//                    Groves.text("tooltip", "groves.chunks.keep_loaded.off", origin.pos().x, origin.pos().z),
//                    (button) -> {
//                        Groves.LOGGER.info("chunkLoadingToggled: ORIGIN -> {}", button.isDisabled());
//                        ClientPlayNetworking.send(new SetChunkLoadingPayload(origin.pos(), button.isFocused()));
//                    });
//            this.originToggleWidget.setDisabled(this.handler.getOrigin().chunkLoad());
//
//            this.listControlWidget = new ChunkDataListControlWidget(x + 1, y + 13, 145, 97);
//
//            this.scrollbarClicked = false;
//            this.scrollPosition = 0.0f;
        }

        public TabChunksWidget addChunks(Collection<GrovesPOI.ClientGroveSanctuary.ChunkData> chunks)
        {
//            this.listControlWidget.addEntries(chunks.stream().map(chunk -> new ChunkDataWidget(this.listControlWidget.getX(), 0, this.listControlWidget.getWidth(), ROW_HEIGHT, chunk, this::chunkLoadingToggled)).toList());

            return this;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.mapWidget.mouseClicked(mouseX, mouseY, button);

//            this.scrollbarClicked = false;
//
//            // Check Scrollbar
//            if (listControlWidget.needsScrollbar() && isPointInBounds(149, 13, 12, 97, mouseX, mouseY))
//            {
//                this.scrollbarClicked = true;
//                return true;
//            }
//
//            // Handle clicks for the origin toggle button
//            if (isPointInControl(this.originToggleWidget, mouseX, mouseY))
//            {
//                if (this.originToggleWidget.mouseClicked(mouseX, mouseY, button))
//                    return true;
//            }
//
//            // Handle mouse clicks down into the list control
//            if (isPointInControl(listControlWidget, mouseX, mouseY))
//            {
//                if (listControlWidget.mouseClicked(mouseX, mouseY, button))
//                    return true;
//            }

//            return false;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            return this.mapWidget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);

//            if (this.scrollbarClicked)
//            {
//                this.scrollPosition = ((float)mouseY - (float)this.listControlWidget.getY() - 7.5F) / (82.0F);
//                this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0F, 1.0F);
//                this.scrollBarY = (int)MathHelper.map(this.scrollPosition, 0.0f, 1.0f, this.listControlWidget.getY(), this.listControlWidget.getBottom() - 15.0f);
//
//                // SCROLLBAR% * HEIGHT_OF_DATA_EXCLUDING_SCROLLBAR_BUTTON
//                int p = (int)(this.scrollPosition * this.listControlWidget.getDataHeight() * (this.listControlWidget.getHeight() - 15.0f) / this.listControlWidget.getHeight());
//                this.listControlWidget.setYOffset(p % ROW_HEIGHT);
//                return true;
//            }


//            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY) {
            this.mapWidget.mouseMoved(mouseX, mouseY);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);

            this.mapWidget.render(context, mouseX, mouseY, delta);

//            // Origin
//            this.originToggleWidget.renderWidget(context, mouseX, mouseY, delta);
//            GrovesPOI.ClientGroveSanctuary.ChunkData origin = this.handler.getSanctuary().getOrigin();
//            context.drawText(MinecraftClient.getInstance().textRenderer, String.format("X = %d, Z = %d", origin.pos().x, origin.pos().z), this.originToggleWidget.getRight() + 2, this.originToggleWidget.getY() + 1, origin.chunkLoad() ? COLOR_ON : COLOR_OFF, false);
//
//            // Additional chunks
//            this.listControlWidget.render(context, mouseX, mouseY, delta);
//
//            // Scrollbar button
//            Identifier bar = this.listControlWidget.needsScrollbar() ? GrovesSanctuaryScreen.SCROLLBAR : GrovesSanctuaryScreen.SCROLLBAR_DISABLED;
//            context.drawGuiTexture(RenderLayer::getGuiTextured, bar, this.getX() + 149, this.getY() + 13 + scrollBarY, 12, 15);
        }

        @Override
        public void renderTooltips(DrawContext context, int mouseX, int mouseY, float delta) {
//            if (isPointInControl(this.originToggleWidget, mouseX, mouseY))
//            {
//                Tooltip tooltip = this.originToggleWidget.getTooltip();
//                if (tooltip != null)
//                    context.drawOrderedTooltip(MinecraftClient.getInstance().textRenderer, tooltip.getLines(MinecraftClient.getInstance()), mouseX, mouseY);
//                return;
//            }
//
//            // Render tooltips all the way down
//            if (isPointInControl(this.listControlWidget, mouseX, mouseY)) {
//                this.listControlWidget.renderTooltips(context, mouseX, mouseY, delta);
//                return;
//            }
        }

        private void chunkLoadingToggled(GrovesPOI.ClientGroveSanctuary.ChunkData chunk, boolean state)
        {
            // Handle the client packet to update the server
            Groves.LOGGER.info("chunkLoadingToggled: {} -> {}", chunk.pos(), state);
            ClientPlayNetworking.send(new SetChunkLoadingPayload(chunk.pos(), state));
        }

        // Called when the handler receives a packet from the server to update the chunk loading status for this chunk.
        public void updateChunkLoading(ChunkPos pos, boolean state)
        {
//            Optional<GrovesPOI.ClientGroveSanctuary.ChunkData> data = this.handler.getChunks().stream().filter(chunk -> chunk.pos().equals(pos)).findFirst();
//
//            data.ifPresent(chunkData -> {
//                chunkData.setLoaded(state);
//                this.listControlWidget.updateChunkLoading(chunkData);
//            });
        }

        public void addChunkData(GrovesPOI.ClientGroveSanctuary.ChunkData chunk)
        {
//            ChunkDataWidget widget = new ChunkDataWidget(this.listControlWidget.getX(), 0, this.listControlWidget.getWidth(), ROW_HEIGHT, chunk, this::chunkLoadingToggled);
//
//            this.listControlWidget.addEntry(widget);
        }

        public void removeChunkData(GrovesPOI.ClientGroveSanctuary.ChunkData chunk)
        {
//            this.listControlWidget.removeEntry(chunk);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {

        }
    }

    static class TabFriendsWidget extends TabControlWidget {
        private final GrovesSanctuaryScreenHandler handler;

        public TabFriendsWidget(int x, int y, GrovesSanctuaryScreenHandler handler) {
            super(ScreenTab.FRIENDS, x, y);
            this.handler = handler;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);
        }

        @Override
        public void renderTooltips(DrawContext context, int mouseX, int mouseY, float delta) {

        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {

        }
    }

    static class TabAbilitiesWidget extends TabControlWidget {
        private final GrovesSanctuaryScreenHandler handler;

        public TabAbilitiesWidget(int x, int y, GrovesSanctuaryScreenHandler handler) {
            super(ScreenTab.ABILITIES, x, y);
            this.handler = handler;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);
        }

        @Override
        public void renderTooltips(DrawContext context, int mouseX, int mouseY, float delta) {

        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {

        }
    }

    static class TabKeybindsWidget extends TabControlWidget {
        private final GrovesSanctuaryScreenHandler handler;

        public TabKeybindsWidget(int x, int y, GrovesSanctuaryScreenHandler handler) {
            super(ScreenTab.KEYBINDS, x, y);
            this.handler = handler;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);
        }

        @Override
        public void renderTooltips(DrawContext context, int mouseX, int mouseY, float delta) {

        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {

        }
    }
}
