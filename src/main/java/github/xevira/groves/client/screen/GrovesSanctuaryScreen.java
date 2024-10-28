package github.xevira.groves.client.screen;

import github.xevira.groves.Groves;
import github.xevira.groves.screenhandler.GrovesSanctuaryScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GrovesSanctuaryScreen extends HandledScreen<GrovesSanctuaryScreenHandler> {
    public static final Identifier BACKGROUND = Groves.id("textures/gui/container/groves_sanctuary_screen.png");

    public GrovesSanctuaryScreen(GrovesSanctuaryScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 0x404040, false);

        long sunlight = this.handler.getSunlight();
        long maxSunlight = this.handler.getMaxSunlight();
        int percent = (int)(100 * sunlight / maxSunlight);
        Text sunlightLabel = Groves.text("label", "groves.sunlight", sunlight, percent);
        context.drawText(this.textRenderer, sunlightLabel, 8, 20, 0x404040, false);

        int foliage = this.handler.getFoliage();
        Text foliageLabel = Groves.text("label", "groves.foliage", foliage, (foliage == 1) ? "" : "s");
        context.drawText(this.textRenderer, foliageLabel, 8, 30, 0x404040, false);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight, 256, 256);
    }
}
