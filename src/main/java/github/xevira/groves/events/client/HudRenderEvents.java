package github.xevira.groves.events.client;

import github.xevira.groves.Groves;
import github.xevira.groves.util.ColorPulser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class HudRenderEvents {

    private static final Identifier METER_OVERLAY = Groves.id("textures/gui/hud/meter_overlay.png");

    private static int sanctuaryRemainingTicks = 0;
    private static int lastAlpha = 255;
    private static final int sanctuaryFadeInTicks = 10;
    private static final int sanctuaryStayTicks = 70;
    private static final int sanctuaryFadeOutTicks = 20;
    private static @Nullable Text sanctuaryMessage;

    private static final MutableText ENTERED_ABANDONED_TEXT = Groves.text("gui", "sanctuary.entered.abandoned");
    private static final MutableText ENTERED_SANCTUARY_TEXT = Groves.text("gui", "sanctuary.entered.yours");
    private static final MutableText ABANDONED_TEXT = Groves.text("gui", "sanctuary.abandoned");
    private static final MutableText SANCTUARY_TEXT = Groves.text("gui", "sanctuary.yours");

    private static Formatting abandonedColor = Formatting.RED;
    private static Formatting othersColor = Formatting.YELLOW;
    private static Formatting yourColor = Formatting.GREEN;

    private static int sunlightPercent = -1;
    private static int darknessPercent = -1;

    private static final ColorPulser darknessColor = new ColorPulser(0xFF400040, 0xFF800080, 0.025f);

    public static void setSunlightPercent(int percent)
    {
        sunlightPercent = Math.min(percent, 100);
    }

    public static void setDarknessPercent(int percent)
    {
        darknessPercent = Math.min(percent, 100);
    }

    public static void setSanctuaryEntry(UUID uuid, String name, String groveName, boolean abandoned, boolean entry)
    {
        if (entry) {
            // When you *ENTER* the particular sanctuary
            if (abandoned) {
                if (groveName.isBlank())
                    sanctuaryMessage = ENTERED_ABANDONED_TEXT.formatted(abandonedColor);
                else
                    sanctuaryMessage = Groves.text("gui", "sanctuary.entered.abandoned.named", groveName).formatted(abandonedColor);
            }
            else if (MinecraftClient.getInstance().player == null || !MinecraftClient.getInstance().player.getUuid().equals(uuid)) {
                if (groveName.isBlank())
                    sanctuaryMessage = Groves.text("gui", "sanctuary.entered.claimed", name).formatted(othersColor);
                else
                    sanctuaryMessage = Groves.text("gui", "sanctuary.entered.claimed.named", name, groveName).formatted(othersColor);
            }
            else {
                if (groveName.isBlank())
                    sanctuaryMessage = ENTERED_SANCTUARY_TEXT.formatted(yourColor);
                else
                    sanctuaryMessage = Groves.text("gui", "sanctuary.entered.yours.named", groveName).formatted(yourColor);
            }
        }
        else {
            // When you first logon in the particular sanctuary

            if (abandoned) {
                if (groveName.isBlank())
                    sanctuaryMessage = ABANDONED_TEXT.formatted(abandonedColor);
                else
                    sanctuaryMessage = Groves.text("gui", "sanctuary.abandoned.named", groveName).formatted(abandonedColor);
            }
            else if (MinecraftClient.getInstance().player == null || !MinecraftClient.getInstance().player.getUuid().equals(uuid)) {
                if (groveName.isBlank())
                    sanctuaryMessage = Groves.text("gui", "sanctuary.claimed", name).formatted(othersColor);
                else
                    sanctuaryMessage = Groves.text("gui", "sanctuary.claimed.named", name, groveName).formatted(othersColor);
            }
            else {
                if (groveName.isBlank())
                    sanctuaryMessage = SANCTUARY_TEXT.formatted(yourColor);
                else
                    sanctuaryMessage = Text.literal(groveName).formatted(yourColor);
            }
        }

        sanctuaryRemainingTicks = sanctuaryFadeInTicks + sanctuaryStayTicks + sanctuaryFadeOutTicks;
        lastAlpha = 255;
    }

    public static void handleTick()
    {
        if (sanctuaryRemainingTicks > 0)
        {
            --sanctuaryRemainingTicks;
            if (sanctuaryRemainingTicks <= 0)
            {
                sanctuaryMessage = null;
            }
        }

        darknessColor.tick();
    }

    public static void renderSanctuaryEntry(DrawContext context, RenderTickCounter tickCounter)
    {
        if (sanctuaryRemainingTicks > 0 && sanctuaryMessage != null) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

            float remaining = (float) sanctuaryRemainingTicks - tickCounter.getTickDelta(false);
            if (remaining <= 0.0f) {
                sanctuaryRemainingTicks = 0;
                return;
            }

            int alpha = 255;
            if (remaining > (sanctuaryStayTicks + sanctuaryFadeOutTicks)) {
                float g = (float) (sanctuaryFadeInTicks + sanctuaryStayTicks + sanctuaryFadeOutTicks) - remaining;
                alpha = (int) (g * 255.0F / (float) sanctuaryFadeInTicks);
                lastAlpha = 255;
            }

            if (remaining <= sanctuaryFadeOutTicks) {
                alpha = (int) (remaining * 255.0F / (float) sanctuaryFadeOutTicks);
            }

            alpha = MathHelper.clamp(alpha, 0, 255);
//            Text debugText = Text.literal(String.format("alpha = %d, ticks = %d", alpha, sanctuaryRemainingTicks));

            if (alpha > 0) {
                lastAlpha = alpha;
                context.getMatrices().push();
                context.getMatrices().translate((float) (context.getScaledWindowWidth() / 2), (float) (3 * context.getScaledWindowHeight() / 4), 0.0F);
                context.getMatrices().scale(1.2F, 1.2F, 1.2F);
                int j = textRenderer.getWidth(sanctuaryMessage);
                int k = ColorHelper.withAlpha(Math.max(alpha, 16), Colors.WHITE);
                context.drawTextWithBackground(textRenderer, sanctuaryMessage, -j / 2, -10, j, k);
                context.getMatrices().pop();
            }
            else
            {
                sanctuaryRemainingTicks = 0;
                sanctuaryMessage = null;
            }
        }
    }

    public static void renderSanctuaryMeters(DrawContext context, RenderTickCounter tickCounter)
    {
        if (sunlightPercent >= 0)
        {
            context.fill(3, 59 - (sunlightPercent/2), 15, 59, 0xFFFFFF00);
            context.drawTexture(RenderLayer::getGuiTextured, METER_OVERLAY, 1, 1, 32, 0, 16, 66, 64, 66);
            context.drawTexture(RenderLayer::getGuiTextured, METER_OVERLAY, 1, 1, 0, 0, 16, 66, 64, 66);
        }

        if (darknessPercent > 0)    // Will only show *IF* the sanctuary has dabbled into darkness.  If it's still at 0, it won't show
        {
            context.drawTexture(RenderLayer::getGuiTextured, METER_OVERLAY, 18, 1, 48, 0, 16, 66, 64, 66);
            context.fill(20, 59 - (darknessPercent/2), 32, 59, darknessColor.getColor());
            context.drawTexture(RenderLayer::getGuiTextured, METER_OVERLAY, 18, 1, 16, 0, 16, 66, 64, 66);
        }

    }
}
