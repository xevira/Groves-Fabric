package github.xevira.groves.client.screen;

import github.xevira.groves.Groves;
import github.xevira.groves.screenhandler.MoonwellScreenHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// 78, 33 x 64, 27 - Glow
// 75, 48 x 70, 25 - Moonwell
// Arrow
// 100, 34 x 18, 18 - Output slot
public class MoonwellScreen extends HandledScreen<MoonwellScreenHandler> {
    public static final Identifier BACKGROUND = Groves.id("textures/gui/container/moonwell_screen.png");

    public MoonwellScreen(MoonwellScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.backgroundWidth = 184;
        this.backgroundHeight = 166;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(BACKGROUND, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        // Night Sky + Moonwell
        if (this.handler.isDay())
            context.drawTexture(BACKGROUND, this.x + 75, this.y + 48, 80, this.backgroundHeight, 80, 25);
        else
            context.drawTexture(BACKGROUND, this.x + 70, this.y + 9, 0, this.backgroundHeight, 80, 64);

        int phase = this.handler.getMoonPhase();
        if (phase < 0)
        {
            // Sun
            context.drawTexture(BACKGROUND, this.x + 96, this.y + 14, this.backgroundWidth, 128, 28, 28);
        }
        else
        {
            // Moon
            context.drawTexture(BACKGROUND, this.x + 102, this.y + 16, this.backgroundWidth, phase * 16, 16, 16);
        }

        // Arrow
        context.drawTexture(BACKGROUND, this.x + 71, this.y + 36, this.backgroundWidth + 16, 18, 24, 16);

        // Output slot
        context.drawTexture(BACKGROUND, this.x + 100, this.y + 34, this.backgroundWidth + 16, 0, 18, 18);

        // Draw the fluid
        SingleFluidStorage fluidStorage = this.handler.getFluidStorage();
        long amount = fluidStorage.getAmount();
        long capacity = fluidStorage.getCapacity();
        Fluid fluid = fluidStorage.getResource().getFluid();
        int fluidBarHeight = Math.round(52.0f * amount / capacity);

        FluidRenderHandler fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);
        if (fluidRenderHandler != null && amount > 0)
        {
            BlockPos pos = this.handler.getBlockEntity().getPos();
            FluidState fluidState = fluid.getDefaultState();
            World world = this.handler.getBlockEntity().getWorld();

            Sprite stillTexture = fluidRenderHandler.getFluidSprites(world, pos, fluidState)[0];
            int tintColor = fluidRenderHandler.getFluidColor(world, pos, fluidState);

            float red = (tintColor >> 16 & 0xFF) / 255.0F;
            float green = (tintColor >> 8 & 0xFF) / 255.0F;
            float blue = (tintColor & 0xFF) / 255.0F;
            context.drawSprite(this.x + 156, this.y + 17 + (52 - fluidBarHeight), 0, 16, fluidBarHeight, stillTexture, red, green, blue, 1.0F);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);

        if (isPointWithinBounds(156, 17, 16, 52, mouseX, mouseY)) {
            SingleFluidStorage fluidStorage = this.handler.getFluidStorage();
            long amount = fluidStorage.getAmount();
            long capacity = fluidStorage.getCapacity();
            Fluid fluid = fluidStorage.getResource().getFluid();
            if (fluid != null && amount > 0) {
                context.drawTooltip(this.textRenderer, Text.translatable(fluid.getDefaultState().getBlockState().getBlock().getTranslationKey()), mouseX, mouseY);
                context.drawTooltip(this.textRenderer, Text.literal(amount + " / " + capacity + " mB"), mouseX, mouseY + 10);
            }
        }
    }
}
