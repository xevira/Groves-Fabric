package github.xevira.groves.client.screen.widget;

import github.xevira.groves.Groves;
import github.xevira.groves.sanctuary.ClientGroveSanctuary;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;

public class ChunkDataWidget extends ClickableTooltipWidget {
    private static int COLOR_OFF = 0x400000;
    private static int COLOR_ON = 0x004000;

    private static final Text TOOLTIP_ON = Groves.text("tooltip", "groves.chunks.keep_loaded.on");
    private static final Text TOOLTIP_OFF = Groves.text("tooltip", "groves.chunks.keep_loaded.off");

    private final ClientGroveSanctuary.ChunkData chunkData;

    private final ToggleButtonWidget chunkLoadToggleButton;
    private final TextRenderer textRenderer;

    private final ChunkLoadingToggled toggled;

    public ChunkDataWidget(int x, int y, int width, int height, ClientGroveSanctuary.ChunkData chunkData, ChunkLoadingToggled toggled) {
        super(x, y, width, height, Text.empty());

        this.textRenderer = MinecraftClient.getInstance().textRenderer;
        this.chunkData = chunkData;
        this.chunkLoadToggleButton = new ToggleButtonWidget(x + 1, y + 1, Text.empty(),
                Groves.text("tooltip", "groves.chunks.keep_loaded.on", chunkData.pos().x, chunkData.pos().z),
                Groves.text("tooltip", "groves.chunks.keep_loaded.off", chunkData.pos().x, chunkData.pos().z),
                this::toggleButtonClicked);
        this.chunkLoadToggleButton.setDisabled(chunkData.chunkLoad());
        this.toggled = toggled;
    }

    public boolean isChunkData(ClientGroveSanctuary.ChunkData chunk)
    {
        return this.chunkData.pos().equals(chunk.pos());
    }

    private void toggleButtonClicked(ToggleButtonWidget button)
    {
        this.chunkData.setLoaded(button.isDisabled());
        if (toggled != null)
            toggled.onToggled(this.chunkData, button.isDisabled());
    }

    @Override
    public void setPosition(int x, int y) {
        super.setPosition(x, y);

        this.chunkLoadToggleButton.setPosition(x + 1, y + 1);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isPointInControl(this.chunkLoadToggleButton, mouseX, mouseY))
        {
            return this.chunkLoadToggleButton.mouseClicked(mouseX, mouseY, button);
        }

        return false;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        this.chunkLoadToggleButton.render(context, mouseX, mouseY, delta);

        context.drawText(this.textRenderer, String.format("X = %d, Z = %d", this.chunkData.pos().x, this.chunkData.pos().z), this.getX() + 12, this.getY() + 2, this.chunkData.chunkLoad() ? COLOR_ON : COLOR_OFF, false);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    public void updateToggle()
    {
        this.chunkLoadToggleButton.setDisabled(this.chunkData.chunkLoad());
    }

    @Override
    public void renderTooltips(DrawContext context, int mouseX, int mouseY, float delta) {
//        if (isPointInControl(this.chunkLoadToggleButton, mouseX, mouseY))
//        {
//            Tooltip tooltip = this.chunkLoadToggleButton.getTooltip();
//            if (tooltip != null)
//                context.drawOrderedTooltip(this.textRenderer, tooltip.getLines(MinecraftClient.getInstance()), mouseX, mouseY);
//        }
    }
}
