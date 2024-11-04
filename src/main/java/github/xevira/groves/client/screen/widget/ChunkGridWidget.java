package github.xevira.groves.client.screen.widget;

import github.xevira.groves.ClientConfig;
import github.xevira.groves.Groves;
import github.xevira.groves.network.ClaimChunkPayload;
import github.xevira.groves.network.SetChunkLoadingPayload;
import github.xevira.groves.poi.GrovesPOI;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;

import java.util.*;

public class ChunkGridWidget extends ClickableTooltipWidget{

    public static final Text AVAILABLE_TOOLTIP = Groves.text("tooltip", "groves.chunk.available");
    public static final Text CLAIMED_TOOLTIP = Groves.text( "tooltip", "groves.chunk.claimed");
    public static final Text ORIGIN_TOOLTIP = Groves.text( "tooltip", "groves.chunk.origin");
    public static final Text KEEP_LOADED_TOOLTIP = Groves.text( "tooltip", "groves.chunk.keep_loaded");
    public static final Text TOGGLE_TOOLTIP = Groves.text( "tooltip", "groves.chunk.toggle");
    public static final Text CLAIM_TOOLTIP = Groves.text( "tooltip", "groves.chunk.claim");

    private static final int GRID_SIZE = 16;

    private GrovesPOI.ClientGroveSanctuary.ChunkData origin;
    private final Map<ChunkPos, GrovesPOI.ClientGroveSanctuary.ChunkData> chunks;
    private final Set<ChunkPos> available;

    private int centerX;
    private int centerY;

    private double xOffset;
    private double yOffset;

    private int columns;
    private int rows;

    private int west;       // Negative X
    private int east;       // Positive X
    private int north;      // Negative Z
    private int south;      // Positive Z

    private ChunkPos mouseOverPos;
    private ScreenRect navigationFocus;

    public ChunkGridWidget(int x, int y, int width, int height, Map<ChunkPos, GrovesPOI.ClientGroveSanctuary.ChunkData> chunks, Set<ChunkPos> available) {
        super(x, y, width, height, Text.empty());

        this.navigationFocus = new ScreenRect(x, y, width, height);
        this.centerX = x + (width - GRID_SIZE) / 2;
        this.centerY = y + (height - GRID_SIZE) / 2;

        this.xOffset = 0;
        this.yOffset = 0;

        this.chunks = chunks;
        this.available = available;

        calculateNEWS();
    }

    private void calculateNEWS()
    {
        this.west = -MathHelper.floor(((this.centerX + (float)this.xOffset) - getX()) / (float)GRID_SIZE + 1);
        this.east = MathHelper.floor((getRight() - (this.centerX + (float)this.xOffset)) / (float)GRID_SIZE + 1);
        this.north = -MathHelper.floor(((this.centerY + (float)this.yOffset) - getY()) / (float)GRID_SIZE + 1);
        this.south = MathHelper.floor((getBottom() - (this.centerY + (float)this.yOffset)) / (float)GRID_SIZE + 1);
    }

    public ChunkGridWidget setOrigin(GrovesPOI.ClientGroveSanctuary.ChunkData origin)
    {
        this.origin = origin;
        return this;
    }

    @Override
    public void renderTooltips(DrawContext context, int mouseX, int mouseY, float delta) {

    }

    private void setNavigationFocus()
    {
        int x = this.centerX + (int)this.xOffset + mouseOverPos.x * GRID_SIZE;
        int y = this.centerY + (int)this.yOffset + mouseOverPos.z * GRID_SIZE;

        this.navigationFocus = new ScreenRect(x, y, GRID_SIZE, GRID_SIZE);
    }

    @Override
    public ScreenRect getNavigationFocus() {
        return this.navigationFocus;
    }

    private void updateTooltip()
    {
        if (this.mouseOverPos == null) return;

        GrovesPOI.ClientGroveSanctuary.ChunkData data = this.chunks.getOrDefault(this.mouseOverPos, null);

        MutableText tooltipText = Groves.text("tooltip", "groves.chunk.location", this.mouseOverPos.x, this.mouseOverPos.z);
        if (data != null)
        {
            tooltipText.append("\n");
            if (data.pos().equals(this.origin.pos()))
                tooltipText.append(ORIGIN_TOOLTIP);
            else
                tooltipText.append(CLAIMED_TOOLTIP);

            if (data.chunkLoad()) {
                tooltipText.append("\n");
                tooltipText.append(KEEP_LOADED_TOOLTIP);
            }

            tooltipText.append("\n\n");
            tooltipText.append(TOGGLE_TOOLTIP);
        }
        else if (this.available.contains(this.mouseOverPos))
        {
            tooltipText.append("\n");
            tooltipText.append(AVAILABLE_TOOLTIP);
            tooltipText.append("\n\n");
            tooltipText.append(CLAIM_TOOLTIP);
        }

        setTooltip(Tooltip.of(tooltipText));
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        double ox = this.centerX + this.xOffset;
        double oy = this.centerY + this.yOffset;

        int x = MathHelper.floor(this.origin.pos().x + (float)((mouseX - ox) / (float)GRID_SIZE));
        int z = MathHelper.floor(this.origin.pos().z + (float)((mouseY - oy) / (float)GRID_SIZE));

        this.mouseOverPos = new ChunkPos(x, z);
        setNavigationFocus();
    }

    private boolean isMapMoving = false;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.isMapMoving = false;
        if (button == 0)
        {
            if (isPointInControl(this, mouseX, mouseY))
            {
                if (Screen.hasShiftDown())
                {
                    // Toggle Chunk load
                    if (this.mouseOverPos != null) {
                        GrovesPOI.ClientGroveSanctuary.ChunkData data = this.chunks.getOrDefault(this.mouseOverPos, null);

                        if (data != null) {
                            data.setLoaded(!data.chunkLoad());
                            ClientPlayNetworking.send(new SetChunkLoadingPayload(data.pos(), data.chunkLoad()));
                            playClickSound(MinecraftClient.getInstance().getSoundManager());
                        }
                    }
                }
                else if (Screen.hasControlDown())
                {
                    // Purchase
                    if (this.mouseOverPos != null) {
                        GrovesPOI.ClientGroveSanctuary.ChunkData data = this.chunks.getOrDefault(this.mouseOverPos, null);

                        if (data == null && this.available.contains(this.mouseOverPos)) {
                            ClientPlayNetworking.send(new ClaimChunkPayload(this.mouseOverPos));
                            playClickSound(MinecraftClient.getInstance().getSoundManager());
                        }
                    }
                }
                else
                    this.isMapMoving = true;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.isMapMoving)
        {
            this.xOffset += deltaX;
            this.yOffset += deltaY;
            calculateNEWS();
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.enableScissor(getX(), getY(), getRight(), getBottom());
        context.getMatrices().push();
        context.getMatrices().translate(this.centerX + this.xOffset, this.centerY + this.yOffset, 0);

        ChunkPos originPos = this.origin.pos();
        for(int ew = this.west; ew <= this.east; ew++)
        {
            for(int ns = this.north; ns <= this.south; ns++)
            {
                ChunkPos pos = new ChunkPos(originPos.x + ew, originPos.z + ns);

                GrovesPOI.ClientGroveSanctuary.ChunkData data = this.chunks.getOrDefault(pos, null);
                int x = ew * GRID_SIZE;
                int y = ns * GRID_SIZE;

                if (data != null)
                {
                    boolean o = (ns == 0 && ew == 0);

                    int color;
                    if (o)
                    {
                        // #E36363
                        if (data.chunkLoad())
                            color = ClientConfig.colorOriginLoaded();
                        else
                            color = ClientConfig.colorOrigin();
                    }
                    else if (data.chunkLoad())
                    {
                        color = ClientConfig.colorChunkLoaded();
                    }
                    else
                    {
                        color = ClientConfig.colorChunkClaimed();
                    }

                    context.fill(x, y, x + GRID_SIZE - 1, y + GRID_SIZE - 1, color);

                    if(o)
                    {
                        context.fill(x+2, y+2, x + GRID_SIZE - 2, y + GRID_SIZE - 2, 0xFF000000);
                        context.fill(x + 3, y + 3, x + GRID_SIZE - 3, y + GRID_SIZE - 3, color);
                    }
                }
                else if (this.available.contains(pos))
                {
                    context.fill(x, y, x + GRID_SIZE - 1, y + GRID_SIZE - 1, ClientConfig.colorChunkAvailable());
                }
            }
        }

        int n = (this.north - 1) * GRID_SIZE;
        int s = (this.south + 1) * GRID_SIZE;
        int e = (this.east + 1) * GRID_SIZE;
        int w = (this.west - 1) * GRID_SIZE;
        for(int ew = this.west; ew <= (this.east + 1); ew++)
        {
            int x = ew * GRID_SIZE;

            context.drawVerticalLine(x, n, s, 0xFF000000);
        }

        for(int ns = this.north; ns <= (this.south + 1); ns++)
        {
            int y = ns * GRID_SIZE;
            context.drawHorizontalLine(w, e, y, 0xFF000000);
        }

        context.getMatrices().pop();
        context.disableScissor();

        updateTooltip();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
