package github.xevira.groves.events.client;

import github.xevira.groves.Groves;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class HudRenderEvents {

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

    public static void setSanctuaryEntry(UUID uuid, String name, String groveName, boolean abandoned, boolean entry)
    {
        Groves.LOGGER.info("setSanctuaryEntry({}, {}, {}, {}, {})", uuid, name, groveName, abandoned, entry);
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

    public static void handleSanctuaryEntryTick()
    {
        if (sanctuaryRemainingTicks > 0)
        {
            --sanctuaryRemainingTicks;
            if (sanctuaryRemainingTicks <= 0)
            {
                sanctuaryMessage = null;
            }
        }
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
}
