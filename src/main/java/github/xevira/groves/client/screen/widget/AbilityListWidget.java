package github.xevira.groves.client.screen.widget;

import com.mojang.authlib.GameProfile;
import github.xevira.groves.Groves;
import github.xevira.groves.sanctuary.GroveAbility;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AbilityListWidget extends ClickableWidget {
    public static final int ROW_HEIGHT = 20;

    private final List<GroveAbility> abilities;
    private final TextRenderer textRenderer;

    private final Set<GroveAbility> selected;
    private GroveAbility hovering;

    private SelectionChanged changed;

    private double offset = 0.0;

    public AbilityListWidget(int x, int y, int width, int height, List<GroveAbility> abilities, SelectionChanged changed) {
        super(x, y, width, height, Text.empty());

        this.abilities = abilities;
        this.textRenderer = MinecraftClient.getInstance().textRenderer;
        this.selected = new HashSet<>();
        this.changed = changed;
    }

    public void setOffset(double offset)
    {
        this.offset = offset;
    }

    public void select(GroveAbility ability)
    {
        if (this.selected.contains(ability))
            this.selected.remove(ability);
        else
            this.selected.add(ability);
        onChanged();
    }

    public void select(int index)
    {
        if (index >= 0 && index < this.abilities.size())
        {
            select(this.abilities.get(index));
        }
    }

    public void clearSelection()
    {
        if (!this.selected.isEmpty()) {
            this.selected.clear();
            onChanged();
        }
    }

    private void onChanged()
    {
        if(this.changed != null)
            this.changed.onSelectionChanged();
    }

    public Iterable<GroveAbility> getSelected()
    {
        return this.selected;
    }

    public int getSelectedCount()
    {
        return this.selected.size();
    }

    public int getListHeight()
    {
        return this.abilities.size() * ROW_HEIGHT;
    }

    private @Nullable GroveAbility mouseSelectProfile(double mouseX, double mouseY)
    {
        double y = this.offset;
        for(GroveAbility ability : this.abilities)
        {
            if (isPointInBounds(0, y, getWidth(), ROW_HEIGHT, mouseX, mouseY))
            {
                return ability;
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

    private void drawAbility(DrawContext context, GroveAbility ability, int x, int y, int width, int height)
    {
        if(this.selected.contains(ability))
        {
            context.fill(x, y, x + width, y + height, 0x7F3F7FFF);
        }
        if(this.hovering == ability)
        {
            context.fill(x, y, x + width, y + height, 0x7FFFFFFF);
        }

        Identifier icon = Groves.id("textures/gui/sprites/abilities/" + ability.getName() + ".png");
        int color = ability.isActive() ? 0xFFFFFFFF : 0xFF808080;
        context.drawTexture(RenderLayer::getGuiTextured, icon, x + 2, y + 2, 0, 0, 16, 16, 16, 16, color);

        // Do Cooldown overlay
        World world = MinecraftClient.getInstance().world;
        if (world != null) {
            if (ability.inCooldown(world)) {
                long total = ability.getEndCooldown() - ability.getStartCooldown();
                long left = ability.getEndCooldown() - world.getTimeOfDay();

                if (left > 0)
                {
                    int h = MathHelper.clamp(((int)(32 * left / total) + 1) / 2, 0, 16);

                    context.fill(x + 2, y + 18 - h, x + 18, y + 18, 0x40FFFFFF);
                }
            }
        }

        int textColor = ability.isActive() ? 0xC0C0C0 : 0x404040;
        context.drawText(this.textRenderer, ability.getNameText(), x + 20, y + (ROW_HEIGHT - this.textRenderer.fontHeight) / 2, textColor, false);

        // What else is needed...
        // - Enabled
        // - Cooldown
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.enableScissor(getX(), getY(), getRight(), getBottom());

        double y = getY() + this.offset;

        for(GroveAbility ability : this.abilities)
        {
            if (y < getBottom() && (y + ROW_HEIGHT) >= getY())
            {
                drawAbility(context, ability, getX(), (int)y, getWidth(), ROW_HEIGHT);
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
