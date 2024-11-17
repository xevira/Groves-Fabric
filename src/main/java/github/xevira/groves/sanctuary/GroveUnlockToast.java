package github.xevira.groves.sanctuary;

import github.xevira.groves.Groves;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;

@Environment(EnvType.CLIENT)
public class GroveUnlockToast implements Toast {
    private static final Identifier TEXTURE = Groves.id("toast/grove_unlock");
    public static final int DEFAULT_DURATION_MS = 5000;

    private final GroveUnlock unlock;
    private boolean soundPlayed;
    private Toast.Visibility visibility = Toast.Visibility.HIDE;


    public GroveUnlockToast(final GroveUnlock unlock)
    {
        this.unlock = unlock;
    }

    @Override
    public Visibility getVisibility() {
        return this.visibility;
    }

    @Override
    public void update(ToastManager manager, long time) {
        if (!this.soundPlayed && time > 0L) {
            this.soundPlayed = true;
            if (this.unlock.isChallenge()) {
                manager.getClient().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F));
            }
        }

        this.visibility = (double)time >= 5000.0 * manager.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long startTime) {
        context.drawGuiTexture(RenderLayer::getGuiTextured, TEXTURE, 0, 0, this.getWidth(), this.getHeight());
        if (this.unlock != null) {
            List<OrderedText> list = textRenderer.wrapLines(this.unlock.getToastTitle(), 125);
            int i = this.unlock.isChallenge() ? -30465 : Colors.YELLOW;
            if (list.size() == 1) {
                context.drawText(textRenderer, this.unlock.getToastText(), 30, 7, i, false);
                context.drawText(textRenderer, (OrderedText)list.get(0), 30, 18, -1, false);
            } else {
                int j = 1500;
                float f = 300.0F;
                if (startTime < 1500L) {
                    int k = MathHelper.floor(MathHelper.clamp((float)(1500L - startTime) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
                    context.drawText(textRenderer, this.unlock.getToastText(), 30, 11, i | k, false);
                } else {
                    int k = MathHelper.floor(MathHelper.clamp((float)(startTime - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
                    int l = this.getHeight() / 2 - list.size() * 9 / 2;

                    for (OrderedText orderedText : list) {
                        context.drawText(textRenderer, orderedText, 30, l, 16777215 | k, false);
                        l += 9;
                    }
                }
            }

            if (this.unlock.getIcon() != null)
                context.drawItemWithoutEntity(this.unlock.getIcon(), 8, 8);
            else if (this.unlock.getIconTexture() != null)
                context.drawGuiTexture(RenderLayer::getGuiTextured, this.unlock.getIconTexture(), 8, 8, 16, 16);
        }
    }
}
