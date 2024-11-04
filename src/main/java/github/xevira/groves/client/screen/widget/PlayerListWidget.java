package github.xevira.groves.client.screen.widget;

import com.mojang.authlib.GameProfile;
import github.xevira.groves.client.SkinCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlayerListWidget extends ClickableWidget {
    public static final int ROW_HEIGHT = 24;

    private final List<GameProfile> profiles;
    private final TextRenderer textRenderer;

    private GameProfile selected;
    private GameProfile hovering;

    private SelectionChanged changed;

    private double offset = 0.0;

    public PlayerListWidget(int x, int y, int width, int height, List<GameProfile> profiles, SelectionChanged changed) {
        super(x, y, width, height, Text.empty());

        this.profiles = profiles;
        this.textRenderer = MinecraftClient.getInstance().textRenderer;
        this.selected = null;
        this.changed = changed;
    }

    public void setOffset(double offset)
    {
        this.offset = offset;
    }

    public void select(GameProfile profile)
    {
        if (profile == null || profiles.contains(profile))
        {
            if (this.selected != profile) {
                this.selected = profile;
                onChanged();
            }
        }
    }

    public void select(int index)
    {
        if (index >= 0 && index < this.profiles.size())
        {
            GameProfile gp = this.profiles.get(index);
            if (this.selected != gp) {
                this.selected = gp;
                onChanged();
            }
        }
    }

    public void clearSelection()
    {
        if (this.selected != null) {
            this.selected = null;
            onChanged();
        }
    }

    private void onChanged()
    {
        if(this.changed != null)
            this.changed.onSelectionChanged();
    }

    public @Nullable GameProfile getSelected()
    {
        return this.selected;
    }

    public int getListHeight()
    {
        return this.profiles.size() * ROW_HEIGHT;
    }

    private @Nullable GameProfile mouseSelectProfile(double mouseX, double mouseY)
    {
        double y = this.offset;
        for(GameProfile profile : this.profiles)
        {
            if (isPointInBounds(0, y, getWidth(), ROW_HEIGHT, mouseX, mouseY))
            {
                return profile;
            }

            y += ROW_HEIGHT;
        }

        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            select(mouseSelectProfile(mouseX, mouseY));
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if(isPointInBounds(0, 0, getWidth(), getHeight(), mouseX, mouseY)) {
            this.hovering = mouseSelectProfile(mouseX, mouseY);
            return;
        }

        this.hovering = null;
    }

    private void drawProfile(DrawContext context, GameProfile profile, int x, int y, int width, int height)
    {
        if(this.selected == profile)
        {
            context.fill(x, y, x + width, y + height, 0x7F3F7FFF);
        }
        if(this.hovering == profile)
        {
            context.fill(x, y, x + width, y + height, 0x7FFFFFFF);
        }

        SkinTextures skinTextures = MinecraftClient.getInstance().getSkinProvider().getSkinTextures(profile);

        int dx;
        if (skinTextures != null) {
            PlayerSkinDrawer.draw(context, skinTextures.texture(), x + 2, y + 2, 16, false, false, -1);
            dx = 24;
        }
        else
            dx = 2;

        context.drawText(this.textRenderer, Text.literal(profile.getName()), x + dx, y + (ROW_HEIGHT - this.textRenderer.fontHeight) / 2, 0x404040, false);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.enableScissor(getX(), getY(), getRight(), getBottom());

        double y = getY() + this.offset;

        for(GameProfile profile : this.profiles)
        {
            if (y < getBottom() && (y + ROW_HEIGHT) >= getY())
            {
                drawProfile(context, profile, getX(), (int)y, getWidth(), ROW_HEIGHT);
            }
            y += ROW_HEIGHT;
        }

        context.disableScissor();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @FunctionalInterface
    public interface SelectionChanged {
        void onSelectionChanged();
    }

    private boolean isPointInBounds(double x, double y, double width, double height, double pointX, double pointY)
    {
        pointX -= this.getX();
        pointY -= this.getY();

        return pointX >= (x - 1) && pointX < (x + width + 1) && pointY >= (y - 1) && pointY < (y + height + 1);
    }
}
