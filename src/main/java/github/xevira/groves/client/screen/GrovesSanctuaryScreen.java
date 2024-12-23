package github.xevira.groves.client.screen;

import com.mojang.authlib.GameProfile;
import github.xevira.groves.ClientConfig;
import github.xevira.groves.Groves;
import github.xevira.groves.client.screen.widget.*;
import github.xevira.groves.network.*;
import github.xevira.groves.sanctuary.ClientGroveSanctuary;
import github.xevira.groves.sanctuary.GroveAbility;
import github.xevira.groves.screenhandler.GrovesSanctuaryScreenHandler;
import github.xevira.groves.util.ColorPulser;
import github.xevira.groves.util.ScreenTab;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class GrovesSanctuaryScreen extends HandledScreen<GrovesSanctuaryScreenHandler> {
    public static final Identifier BACKGROUND = Groves.id("textures/gui/container/sanctuary/background.png");

    public static final Identifier SCROLLBAR = Groves.id("scrollbar");
    public static final Identifier SCROLLBAR_DISABLED = Groves.id("scrollbar_disabled");

    private final int tabBottomY;

    private final int tabBackgroundWidth;
    private final int tabBackgroundHeight;

//    private final Map<ScreenTab, List<Drawable>> tabControls = new HashMap<>();

    private final List<TabControlWidget> Tabs = new ArrayList<>();
    private TabControlWidget currentTab;

    public GrovesSanctuaryScreen(GrovesSanctuaryScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.tabBottomY = 13;

        this.backgroundHeight = 179;

        this.tabBackgroundWidth = 162;
        this.tabBackgroundHeight = 131;

//        List<ClientGroveSanctuary.ChunkData> chunks = this.handler.getSanctuary().getChunks();
//        for(ClientGroveSanctuary.ChunkData chunk : chunks)
//        {
//            Groves.LOGGER.info("Screen: chunk = {} ({})", chunk.pos(), chunk.chunkLoad());
//        }
    }

    @Override
    protected void init() {
        super.init();

//        TextFieldWidget test = new TextFieldWidget(this.textRenderer, this.x + this.backgroundWidth - 70, this.y + 21, 50, 12, Text.empty());
//        test.setFocusUnlocked(false);
//        test.setEditableColor(-1);
//        test.setUneditableColor(-1);
//        test.setDrawsBackground(true);
//        test.setMaxLength(50);
//        this.addDrawableChild(test);

        Tabs.add(new TabGeneralWidget(this.x + 7, this.y + 41, this.handler));
        Tabs.add(new TabChunksWidget(this.x + 7, this.y + 41, this.handler));
        if (!this.handler.getSanctuary().isAbandoned()) {
            if (isMultiplayer()) {
                Tabs.add(new TabFriendsWidget(this.x + 7, this.y + 41, this.handler));
            }
            Tabs.add(new TabAbilitiesWidget(this.x + 7, this.y + 41, this.handler));
        }
        Tabs.add(new TabKeybindsWidget(this.x + 7, this.y + 41, this.handler));

        for(TabControlWidget widget : Tabs)
        {
            //this.addDrawable(widget);
            widget.setScreen(this);

            for(ClickableWidget clickable : widget.getChildren())
                this.addDrawableChild(clickable);

            widget.setVisible(false);
        }

        this.currentTab = Tabs.getFirst();
        handler.setCurrentTab(this.currentTab.getTab());
        this.currentTab.setVisible(true);
    }



    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();

        this.currentTab.tick();
    }

    private boolean isMultiplayer()
    {
        MinecraftServer server = MinecraftClient.getInstance().getServer();
        ServerInfo serverInfo = MinecraftClient.getInstance().getCurrentServerEntry();

        if (ClientConfig.isDevMode())
            return true;

        // Singleplayer
        if (server != null && !server.isRemote())
            return false;

        // Realm
        if (serverInfo != null && serverInfo.isRealm())
            return true;

        // External Multiplayer
        if (server == null && (serverInfo == null || !serverInfo.isLocal()))
            return true;

        // LAN
        return true;
    }

    private void selectTab(TabControlWidget tab) {
        if (!this.handler.isSelectedTab(tab.getTab())) {
            this.handler.setCurrentTab(tab.getTab());
            tab.setVisible(true);
            this.currentTab.setVisible(false);
            this.currentTab = tab;
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.getMatrices().push();
        context.getMatrices().translate(0, this.tabBottomY, 0);

        String groveName = this.handler.getSanctuary().getGroveName();
        if (groveName.isBlank() || groveName.isEmpty())
            context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 0x404040, false);
        else
            context.drawText(this.textRenderer, Text.literal(groveName), this.titleX, this.titleY, 0x404040, false);
        context.drawText(this.textRenderer, this.currentTab.getLabelText(), this.titleX, this.titleY + this.textRenderer.fontHeight + 2, 0x404040, false);

        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = 3;
        for(TabControlWidget tab : Tabs) {
            if (isPointWithinBounds(x, 0, 26, 13, mouseX, mouseY))
            {
                selectTab(tab);
                return true;
            }
            x += 26;
        }

        if (this.currentTab.mouseClicked(mouseX, mouseY, button))
            return true;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.currentTab.mouseDragged(mouseX, mouseY, button, deltaX, deltaY))
            return true;

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
        else
            this.currentTab.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.client != null && this.client.player != null) {
            this.client.player.closeHandledScreen();
            return true;
        }

        if (this.currentTab.keyPressed(keyCode, scanCode, modifiers))
            return true;

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
//
//    @Override
//    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
//        if (this.currentTab.keyReleased(keyCode, scanCode, modifiers))
//            return true;
//
//        return super.keyReleased(keyCode, scanCode, modifiers);
//    }

//    @Override
//    public boolean charTyped(char chr, int modifiers) {
//        Groves.LOGGER.info("charTyped: {}, {}", chr, modifiers);
//
//        if (this.currentTab.charTyped(chr, modifiers))
//            return true;
//
//        return super.charTyped(chr, modifiers);
//    }

    public void drawTab(DrawContext context, TabControlWidget tab, int x, boolean selected) {
        int index = tab.getTab().ordinal();

        // Tab background
        context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, this.x + x, this.y, this.backgroundWidth, selected ? 0 : 16, 26, 16, 256, 256);

        // Tab icon
        context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, this.x + x + 4, this.y + 4, this.backgroundWidth + (selected ? 0 : 18), 32 + 8 * index, 18, 8, 256, 256);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight, 256, 256);

        // Render tab .. tabs
        int tabX = 3;
        for(TabControlWidget tab : Tabs)
        {
            drawTab(context, tab, tabX, this.currentTab == tab);

            tabX += 26;
        }

        this.currentTab.render(context, mouseX, mouseY, delta);

        // Render status ribbon
        if (this.handler.getAbilities().stream().anyMatch(GroveAbility::isActive))
        {
            // Opener
            context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, this.x + 6, this.y + this.backgroundHeight - 3, this.backgroundWidth + 26, 0, 4, 16, 256, 256);

            int x = this.x + 10;
            for(GroveAbility ability : this.handler.getAbilities())
            {
                if (ability.isActive())
                {
                    context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, x, this.y + this.backgroundHeight - 3, this.backgroundWidth + 30, 0, 13, 16, 256, 256);

                    Identifier icon = Groves.id("textures/gui/sprites/abilities/icon/" + ability.getName() + ".png");
                    context.drawTexture(RenderLayer::getGuiTextured, icon, x + 1, this.y + backgroundHeight - 3, 0, 0, 12, 12, 12, 12);
                    x += 13;
                }
            }

            // Closer
            context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, x, this.y + this.backgroundHeight - 3, this.backgroundWidth + 43, 0, 5, 16, 256, 256);
        }

    }

    private void drawErrorMessage(DrawContext context)
    {
        int errorTicks = this.handler.getErrorMessageTicks();
        if (errorTicks > 0 && this.handler.getErrorMessage() != null)
        {
            int alpha = 255;
            if (errorTicks > 90)
            {
                alpha = (int)(25.5F * (100 - errorTicks));
            }
            else if (errorTicks < 20)
            {
                alpha = (int)(255.0f * errorTicks / 20);
            }

            alpha = Math.max(alpha, 16);
            int color = (alpha << 24) | 0xFF0000;
            int backcolor = (alpha << 24) | 0x400000;
            int bordercolor = (alpha << 24);

            int w = this.textRenderer.getWidth(this.handler.getErrorMessage()) + 20;
            int h = this.textRenderer.fontHeight + 10;
            int x = (this.width - w) / 2;
            int y = (this.height - h) / 2 + 5;

            context.fill(x + 1, y + 1, x + w - 1, y + h - 1, backcolor);
            context.drawVerticalLine(x, y, y + h, bordercolor);
            context.drawVerticalLine(x + w, y, y + h, bordercolor);
            context.drawHorizontalLine(x, x + w, y, bordercolor);
            context.drawHorizontalLine(x, x + w, y + h, bordercolor);

            context.drawCenteredTextWithShadow(this.textRenderer, this.handler.getErrorMessage(), this.width / 2, this.height / 2, color);
        }

        this.handler.tick();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Drawables
        super.render(context, mouseX, mouseY, delta);

        // Tooltips
        drawMouseoverTooltip(context, mouseX, mouseY);

        this.currentTab.renderTooltips(context, mouseX, mouseY, delta);

        drawErrorMessage(context);
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

            List<ClientGroveSanctuary.ChunkData> chunks = this.handler.getChunks();
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
                ClientGroveSanctuary.ChunkData chunk = chunks.get(i);

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

            List<ClientGroveSanctuary.ChunkData> chunks = this.handler.getChunks();
            int pixels = chunks.size() * ROW_HEIGHT;
            Identifier bar = (pixels > 97) ? GrovesSanctuaryScreen.SCROLLBAR : GrovesSanctuaryScreen.SCROLLBAR_DISABLED;
            context.drawGuiTexture(RenderLayer::getGuiTextured, bar, this.wx + this.x + 149, this.wy + this.y + 13 + scrollBarY, 12, 15);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.scrollbarClicked = false;
            List<ClientGroveSanctuary.ChunkData> chunks = this.handler.getChunks();
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
        public static final Text NAME_TEXT = Groves.text("label", "groves.name");
        public static final Text NAME_SET_TEXT = Groves.text("button", "groves.name.set");
        public static final Text SUNLIGHT_TEXT = Groves.text("label", "groves.sunlight");
        public static final Text DARKNESS_TEXT = Groves.text("label", "groves.darkness");
        public static final Text FOLIAGE_TEXT = Groves.text("label", "groves.foliage");
        public static final Text MOONWELL_TEXT = Groves.text("label", "groves.moonwell");
        public static final Text NO_MOONWELL_TEXT = Groves.text("text", "groves.no_moonwell");

        public static final Text SPAWN_TEXT = Groves.text("label", "groves.spawn");
        public static final Text SPAWN_SET_TEXT = Groves.text("button", "groves.spawn.set");

        private final int nameLabelW;
        private final int sunlightLabelW;
        private final int darknessLabelW;
        private final int foliageLabelW;
        private final int moonwellLabelW;
        private final int spawnLabelW;
        private final int spawnButtonW;

        private final TextRenderer textRenderer;

        private final GrovesSanctuaryScreenHandler handler;

        private final TextFieldWidget groveNameField;
        private final ButtonWidget setGroveNameButton;
        private final ButtonWidget setSpawnPointButton;

        private boolean groveNameFieldFocused = false;

        private String groveName;


        private final ColorPulser darknessColor = new ColorPulser(0xFF400040, 0xFF800080, 0.025f);

        public TabGeneralWidget(int x, int y, GrovesSanctuaryScreenHandler handler) {
            super(ScreenTab.GENERAL, x, y);

            this.textRenderer = MinecraftClient.getInstance().textRenderer;

            this.handler = handler;
            this.groveName = handler.getSanctuary().getGroveName();

            this.nameLabelW = this.textRenderer.getWidth(NAME_TEXT.asOrderedText());
            this.sunlightLabelW = this.textRenderer.getWidth(SUNLIGHT_TEXT.asOrderedText());
            this.darknessLabelW = this.textRenderer.getWidth(DARKNESS_TEXT.asOrderedText());
            this.foliageLabelW = this.textRenderer.getWidth(FOLIAGE_TEXT.asOrderedText());
            this.moonwellLabelW = this.textRenderer.getWidth(MOONWELL_TEXT.asOrderedText());
            this.spawnLabelW = this.textRenderer.getWidth(SPAWN_TEXT.asOrderedText());
            this.spawnButtonW = this.textRenderer.getWidth(SPAWN_SET_TEXT.asOrderedText());


            this.groveNameField = new TextFieldWidget(this.textRenderer,x + nameLabelW + 10, y + 5, 50, 12, Text.empty());
            this.groveNameField.setFocusUnlocked(false);
            this.groveNameField.setEditableColor(-1);
            this.groveNameField.setUneditableColor(-1);
            this.groveNameField.setDrawsBackground(true);
            this.groveNameField.setMaxLength(50);
            this.groveNameField.active = true;
            this.groveNameField.setText(this.handler.getSanctuary().getGroveName());

            this.setGroveNameButton = ButtonWidget.builder(NAME_SET_TEXT, (button) -> {
                ClientPlayNetworking.send(new SetGroveNamePayload(this.groveNameField.getText()));
            }).dimensions(x + nameLabelW + 60, y + 5, 50, 12).build();

            this.setSpawnPointButton = ButtonWidget.builder(SPAWN_SET_TEXT, (button) -> {
                if (MinecraftClient.getInstance().player != null)
                    ClientPlayNetworking.send(new SetSpawnPointPayload(MinecraftClient.getInstance().player.getBlockPos()));
            }).dimensions(x + 5, y + 96, spawnButtonW + 10, 16).build();

            this.addChildElement(this.groveNameField);
            this.addChildElement(this.setGroveNameButton);
            this.addChildElement(this.setSpawnPointButton);
        }

        @Override
        public void tick() {
            this.darknessColor.tick();
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

        private void drawDarknessProgressBar(DrawContext context, int x, int y) {
            long darkness = this.handler.getDarkness();
            long maxDarkness = this.handler.getMaxDarkness();
            int percent = (int) (100 * darkness / maxDarkness);

            context.drawText(this.textRenderer, DARKNESS_TEXT, x, y + 1, 0x404040, false);

            // Background of the bar
            context.drawTexture(RenderLayer::getGuiTextured, texture, x + darknessLabelW + 2, y, 0, TAB_HEIGHT, 102, 10, 256, 256);

            // Foreground of the bar
            context.fill(x + darknessLabelW + 3, y + 1, x + darknessLabelW + 3 + percent, y + 9, this.darknessColor.getColor());
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

        private void drawSpawnPoint(DrawContext context, int x, int y) {
            context.drawText(this.textRenderer, SPAWN_TEXT, x, y, 0x404040, false);
            BlockPos pos = this.handler.getSanctuary().getSpawnPoint();
            Text spawnText = Groves.text("text", "groves.spawn", pos.getX(), pos.getY(), pos.getZ());
            context.drawText(this.textRenderer, spawnText, x + spawnLabelW + 2, y, 0x404040, false);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);

            context.drawText(this.textRenderer, NAME_TEXT, this.getX() + 5, this.getY() + 5, 0x404040, false);

            drawSunlightProgressBar(context, this.getX() + 5, this.getY() + 21);
            drawDarknessProgressBar(context, this.getX() + 5, this.getY() + 37);
            drawFoliage(context, this.getX() + 5, this.getY() + 53);
            drawMoonwell(context, this.getX() + 5, this.getY() + 69);
            drawSpawnPoint(context, this.getX() + 5, this.getY() + 85);
        }

        @Override
        public void renderTooltips(DrawContext context, int mouseX, int mouseY, float delta) {
            if (isPointInBounds(sunlightLabelW + 6, 22, 100, 8, mouseX, mouseY)) {
                long sunlight = this.handler.getSunlight();
                long maxSunlight = this.handler.getMaxSunlight();
                long total = this.handler.getTotalSunlight();
                int percent = (int) (100 * sunlight / maxSunlight);

                List<Text> tooltips = new ArrayList<>();
                tooltips.add(Groves.text("tooltip", "groves.sunlight", sunlight, percent).formatted(Formatting.YELLOW));
                tooltips.add(Groves.text("tooltip", "groves.sunlight.collected", total).formatted(Formatting.YELLOW));
                context.drawTooltip(this.textRenderer, tooltips, mouseX, mouseY);
            }
            if (isPointInBounds(darknessLabelW + 6, 38, 100, 8, mouseX, mouseY)) {
                long darkness = this.handler.getDarkness();
                long maxDarkness = this.handler.getMaxDarkness();
                long total = this.handler.getTotalDarkness();
                int percent = (int) (100 * darkness / maxDarkness);

                List<Text> tooltips = new ArrayList<>();
                tooltips.add(Groves.text("tooltip", "groves.darkness", darkness, percent).formatted(Formatting.LIGHT_PURPLE));
                tooltips.add(Groves.text("tooltip", "groves.darkness.collected", total).formatted(Formatting.LIGHT_PURPLE));
                context.drawTooltip(this.textRenderer, tooltips, mouseX, mouseY);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
//            this.groveNameFieldFocused = false;
//            if (isPointInControl(this.setSpawnPointButton, mouseX, mouseY))
//            {
//                this.screen.setFocused(null);
//                return this.setSpawnPointButton.mouseClicked(mouseX, mouseY, button);
//            }
//            else if (isPointInControl(this.setGroveNameButton, mouseX, mouseY))
//            {
//                this.screen.setFocused(null);
//                return this.setGroveNameButton.mouseClicked(mouseX, mouseY, button);
//            }
//            else if (isPointInControl(this.groveNameField, mouseX, mouseY))
//            {
//                Groves.LOGGER.info("Clicked on name field");
//                this.screen.setFocused(this.groveNameField);
//                this.groveNameFieldFocused = true;
//                return this.groveNameField.mouseClicked(mouseX, mouseY, button);
//            }
//            else {
//                this.screen.setFocused(null);
//                return super.mouseClicked(mouseX, mouseY, button);
//            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return this.groveNameField.keyPressed(keyCode, scanCode, modifiers) || this.groveNameField.isActive() || super.keyPressed(keyCode, scanCode, modifiers);
        }
//
//        @Override
//        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
//            if (this.groveNameFieldFocused)
//                return this.groveNameField.keyReleased(keyCode, scanCode, modifiers);
//
//            return false;
//        }
//
//        @Override
//        public boolean charTyped(char chr, int modifiers) {
//            if (this.groveNameFieldFocused)
//                return this.groveNameField.charTyped(chr, modifiers);
//
//            return super.charTyped(chr, modifiers);
//        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {

        }
    }

    static class TabChunksWidget extends TabControlWidget {
        private final GrovesSanctuaryScreenHandler handler;

        private final ChunkGridWidget mapWidget;

        public TabChunksWidget(int x, int y, GrovesSanctuaryScreenHandler handler) {
            super(ScreenTab.CHUNKS, x, y);
            this.handler = handler;

            this.mapWidget = new ChunkGridWidget(x + 1, y + 1, TAB_WIDTH - 2, TAB_HEIGHT - 2, this.handler.getChunkMap(), this.handler.getAvailableChunks())
                    .setOrigin(this.handler.getOrigin());

            this.addChildElement(this.mapWidget);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
//            return this.mapWidget.mouseClicked(mouseX, mouseY, button);
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            return this.mapWidget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY) {
            if (isPointInControl(this.mapWidget, mouseX, mouseY))
                this.mapWidget.mouseMoved(mouseX, mouseY);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);

            this.mapWidget.render(context, mouseX, mouseY, delta);
        }

        @Override
        public void renderTooltips(DrawContext context, int mouseX, int mouseY, float delta) {
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {

        }
    }

    static class TabFriendsWidget extends TabControlWidget {
        private final GrovesSanctuaryScreenHandler handler;

        private final PlayerListWidget playerListWidget;
        private final ButtonWidget addFriendWidget;
        private final TextFieldWidget addFriendNameWidget;
        private final ButtonWidget removeFriendWidget;

        private boolean scrollbarClicked = false;
        private int scrollbarY = 0;
        private double scrollbarYClicked = 0.0D;

        public TabFriendsWidget(int x, int y, GrovesSanctuaryScreenHandler handler) {
            super(ScreenTab.FRIENDS, x, y);
            this.handler = handler;

            this.playerListWidget = new PlayerListWidget(x + 1, y + 1, 145, 89, this.handler.getSanctuary().getFriends(), this::onSelectionChanged);

            this.addFriendWidget = ButtonWidget.builder(Text.literal("Add"), this::onAddFriendClicked)
                    .dimensions(x + 1, this.playerListWidget.getBottom() + 2, 50, 12).build();

            this.removeFriendWidget = ButtonWidget.builder(Text.literal("Remove"), this::onRemoveFriendClicked)
                    .dimensions(x + 1, this.addFriendWidget.getBottom() + 2, 50, 12).build();

            this.addFriendNameWidget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, this.addFriendWidget.getRight() + 2, this.addFriendWidget.getY(), 75, 12, Text.empty());
            this.addFriendNameWidget.setFocusUnlocked(false);
            this.addFriendNameWidget.setEditableColor(-1);
            this.addFriendNameWidget.setUneditableColor(-1);
            this.addFriendNameWidget.setDrawsBackground(true);
            this.addFriendNameWidget.setMaxLength(50);
            this.addFriendNameWidget.active = true;
            this.addFriendNameWidget.setChangedListener(s -> {
                this.addFriendWidget.active = !(s.isEmpty() || s.isBlank());
            });
            this.addFriendNameWidget.setText("");

            this.addChildElement(this.playerListWidget);
            this.addChildElement(this.addFriendWidget);
            this.addChildElement(this.addFriendNameWidget);
            this.addChildElement(this.removeFriendWidget);
        }

        @Override
        public void setVisible(boolean visible) {
            super.setVisible(visible);

            this.addFriendNameWidget.setText("");
            this.addFriendNameWidget.active = true;
            this.addFriendWidget.active = false;
            this.playerListWidget.clearSelection();
            this.removeFriendWidget.active = false;
        }

        private void onSelectionChanged()
        {
            this.removeFriendWidget.active = (this.playerListWidget.getSelected() != null);
        }

        private void onAddFriendClicked(ButtonWidget button)
        {
            String name = this.addFriendNameWidget.getText();
            this.addFriendNameWidget.setText("");

            ClientPlayNetworking.send(new AddFriendPayload(name));
        }

        private void onRemoveFriendClicked(ButtonWidget button)
        {
            GameProfile removal = this.playerListWidget.getSelected();

            if (removal != null)
            {
                Groves.LOGGER.info("REMOVE: {} -> {}", removal.getId(), removal.getName());
                ClientPlayNetworking.send(new RemoveFriendPayload(removal.getId()));
                this.playerListWidget.clearSelection();
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.scrollbarClicked = false;
            int pixels = this.playerListWidget.getListHeight();
            if (pixels > this.playerListWidget.getHeight() && isPointInBounds(149, scrollbarY + 1, 15, 12, mouseX, mouseY))
            {
                Groves.LOGGER.info("scroll bar clicked");
                this.scrollbarYClicked = mouseY - (this.playerListWidget.getY() + scrollbarY);
                this.scrollbarClicked = true;
                return true;
            }

            if (isPointInControl(this.playerListWidget, mouseX, mouseY))
            {
                this.playerListWidget.mouseClicked(mouseX, mouseY, button);
                return true;
            }

            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (this.scrollbarClicked)
            {
                int j = this.playerListWidget.getY();
                int k = this.playerListWidget.getBottom();
                double scrollPosition = ((mouseY - scrollbarYClicked - j) / (this.playerListWidget.getHeight() - 15.0));
                scrollPosition = MathHelper.clamp(scrollPosition, 0.0, 1.0);
                this.scrollbarY = (int)Math.round((this.playerListWidget.getHeight() - 15.0) * scrollPosition);
                return true;
            }

            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY) {

            if (isPointInControl(this.playerListWidget, mouseX, mouseY))
            {
                this.playerListWidget.mouseMoved(mouseX, mouseY);
                return;
            }

            super.mouseMoved(mouseX, mouseY);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return this.addFriendNameWidget.keyPressed(keyCode, scanCode, modifiers) || this.addFriendNameWidget.isActive() || super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);

            // Draw scrollbar
            Identifier bar = (this.playerListWidget.getListHeight() > this.playerListWidget.getHeight()) ? GrovesSanctuaryScreen.SCROLLBAR : GrovesSanctuaryScreen.SCROLLBAR_DISABLED;
            context.drawGuiTexture(RenderLayer::getGuiTextured, bar, this.getRight() - 13, this.playerListWidget.getY() + this.scrollbarY, 12, 15);

            this.playerListWidget.render(context, mouseX, mouseY, delta);
        }

        @Override
        public void renderTooltips(DrawContext context, int mouseX, int mouseY, float delta) {

        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {

        }
    }

    static class TabAbilitiesWidget extends TabControlWidget {
        public static final Text START_TEXT = Groves.text("button", "start");
        public static final Text STOP_TEXT = Groves.text("button", "stop");
        public static final Text USE_TEXT = Groves.text("button", "use");

        private final GrovesSanctuaryScreenHandler handler;

        private final AbilityListWidget abilityListWidget;
        private final ButtonWidget startButtonWidget;
        private final ButtonWidget stopButtonWidget;
        private final ButtonWidget useButtonWidget;

        private boolean scrollbarClicked = false;
        private int scrollbarY = 0;
        private double scrollbarYClicked = 0.0D;

        public TabAbilitiesWidget(int x, int y, GrovesSanctuaryScreenHandler handler) {
            super(ScreenTab.ABILITIES, x, y);
            this.handler = handler;

            this.abilityListWidget = new AbilityListWidget(x + 1, y + 1, 145, TAB_HEIGHT - 22, this.handler.getAbilities(), this::onSelectionChanged);

            this.startButtonWidget = ButtonWidget.builder(START_TEXT, this::onStart)
                    .dimensions(x + 1, this.abilityListWidget.getBottom() + 1, 40, 18)
                    .build();
            this.startButtonWidget.active = false;

            this.stopButtonWidget = ButtonWidget.builder(STOP_TEXT, this::onStop)
                    .dimensions(this.startButtonWidget.getRight() + 1, this.abilityListWidget.getBottom() + 1, 40, 18)
                    .build();
            this.stopButtonWidget.active = false;

            this.useButtonWidget = ButtonWidget.builder(USE_TEXT, this::onUse)
                    .dimensions(this.stopButtonWidget.getRight() + 1, this.abilityListWidget.getBottom() + 1, 40, 18)
                    .build();
            this.useButtonWidget.active = false;

            this.addChildElement(this.abilityListWidget);
            this.addChildElement(this.startButtonWidget);
            this.addChildElement(this.stopButtonWidget);
            this.addChildElement(this.useButtonWidget);
        }

        private void onSelectionChanged()
        {
            boolean start = false;
            boolean stop = false;
            boolean use = false;
            if (this.abilityListWidget.getSelectedCount() > 0) {
                for (GroveAbility ability : this.abilityListWidget.getSelected()) {
                    if (ability == null) continue;
                    if (ability.isAutomatic()) {
                        if (ability.isActive())
                            stop = true;
                        else if (ability.isEnabled())
                            start = true;
                    } else if (ability.isEnabled())
                        use = true;
                }
            }

            this.startButtonWidget.active = start;
            this.stopButtonWidget.active = stop;
            this.useButtonWidget.active = use;

        }

        private void onStart(ButtonWidget button)
        {
            for (GroveAbility ability : this.abilityListWidget.getSelected()) {
                if (ability.isAutomatic() && ability.isEnabled() && !ability.isActive())
                {
                    ClientPlayNetworking.send(new StartGroveAbitlityPayload(ability.getName()));
                }
            }

            this.abilityListWidget.clearSelection();
        }

        private void onStop(ButtonWidget button)
        {
            for (GroveAbility ability : this.abilityListWidget.getSelected()) {
                if (ability.isAutomatic() && ability.isEnabled() && ability.isActive())
                {
                    ClientPlayNetworking.send(new StopGroveAbitlityPayload(ability.getName()));
                }
            }

            this.abilityListWidget.clearSelection();
        }

        private void onUse(ButtonWidget button)
        {
            for (GroveAbility ability : this.abilityListWidget.getSelected()) {
                if (!ability.isAutomatic() && ability.isEnabled())
                {
                    ClientPlayNetworking.send(new UseGroveAbitlityPayload(ability.getName()));
                }
            }

            this.abilityListWidget.clearSelection();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.scrollbarClicked = false;
            int pixels = this.abilityListWidget.getListHeight();
            if (pixels > this.abilityListWidget.getHeight() && isPointInBounds(149, scrollbarY + 1, 15, 12, mouseX, mouseY))
            {
                Groves.LOGGER.info("scroll bar clicked");
                this.scrollbarYClicked = mouseY - (this.abilityListWidget.getY() + scrollbarY);
                this.scrollbarClicked = true;
                return true;
            }

            if (isPointInControl(this.abilityListWidget, mouseX, mouseY))
            {
                this.abilityListWidget.mouseClicked(mouseX, mouseY, button);
                return true;
            }

            return super.mouseClicked(mouseX, mouseY, button);
        }


        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (this.scrollbarClicked)
            {
                int j = this.abilityListWidget.getY();
                int k = this.abilityListWidget.getBottom();
                double scrollPosition = ((mouseY - scrollbarYClicked - j) / (this.abilityListWidget.getHeight() - 15.0));
                scrollPosition = MathHelper.clamp(scrollPosition, 0.0, 1.0);
                this.scrollbarY = (int)Math.round((this.abilityListWidget.getHeight() - 15.0) * scrollPosition);
                return true;
            }

            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY) {
            if (isPointInControl(this.abilityListWidget, mouseX, mouseY))
            {
                this.abilityListWidget.mouseMoved(mouseX, mouseY);
                return;
            }

            super.mouseMoved(mouseX, mouseY);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);

            // Draw scrollbar
            Identifier bar = (this.abilityListWidget.getListHeight() > this.abilityListWidget.getHeight()) ? GrovesSanctuaryScreen.SCROLLBAR : GrovesSanctuaryScreen.SCROLLBAR_DISABLED;
            context.drawGuiTexture(RenderLayer::getGuiTextured, bar, this.getRight() - 13, this.abilityListWidget.getY() + this.scrollbarY, 12, 15);

            this.abilityListWidget.render(context, mouseX, mouseY, delta);
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
