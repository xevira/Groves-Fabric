package github.xevira.groves.screenhandler;

import com.google.common.collect.ImmutableList;
import github.xevira.groves.Registration;
import github.xevira.groves.block.entity.MoonwellMultiblockMasterBlockEntity;
import github.xevira.groves.network.GrovesSanctuaryScreenPayload;
import github.xevira.groves.network.MoonwellScreenPayload;
import github.xevira.groves.poi.GrovesPOI;
import github.xevira.groves.util.ISlotVisibility;
import github.xevira.groves.util.ScreenTab;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrovesSanctuaryScreenHandler extends ScreenHandler {
    private final GrovesPOI.ClientGroveSanctuary sanctuary;
    private ScreenTab currentTab;

    private Map<ChunkPos, GrovesPOI.ClientGroveSanctuary.ChunkData> chunks = new HashMap<>();

    private Text buyChunkReason = Text.empty();
    private int buyChunkTicks = 0;

    public GrovesSanctuaryScreenHandler(int syncId, PlayerInventory playerInventory, GrovesSanctuaryScreenPayload payload)
    {
        this(syncId, playerInventory, payload.sanctuary());
    }

    public GrovesSanctuaryScreenHandler(int syncId, PlayerInventory playerInventory, GrovesPOI.ClientGroveSanctuary sanctuary)
    {
        super(Registration.GROVES_SANCTUARY_SCREEN_HANDLER, syncId);

        this.sanctuary = sanctuary;
        this.currentTab = ScreenTab.GENERAL;

        putChunk(sanctuary.getOrigin());

        for(GrovesPOI.ClientGroveSanctuary.ChunkData chunk : sanctuary.getChunks())
            putChunk(chunk);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        // Only way to get to this is if you are the owner of the sanctuary
        return true;
    }

    public GrovesPOI.ClientGroveSanctuary getSanctuary()
    {
        return this.sanctuary;
    }

    /////////////////////////
    // Tabs
    //
    // -- General
    public int getFoliage()
    {
        return this.sanctuary.getFoliage();
    }

    public long getSunlight()
    {
        return this.sanctuary.getStoredSunlight();
    }

    public long getMaxSunlight()
    {
        return this.sanctuary.getMaxStoredSunlight();
    }

    public @Nullable BlockPos getMoonwell()
    {
        return this.sanctuary.getMoonwell();
    }


    // TODO: Add update methods for the screen
    public void setSunlight(long sunlight)
    {
        this.sanctuary.setStoredSunlight(sunlight);
    }

    public void setFoliage(int foliage)
    {
        this.sanctuary.setFoliage(foliage);
    }

    public void setMoonwell(BlockPos pos)
    {
        this.sanctuary.setMoonwell(pos);
    }

    //
    // -- Chunks
    public boolean isChunkLoading()
    {
        return this.sanctuary.isChunkLoading();
    }

    public GrovesPOI.ClientGroveSanctuary.ChunkData getOrigin()
    {
        return this.sanctuary.getOrigin();
    }

    public List<GrovesPOI.ClientGroveSanctuary.ChunkData> getChunks()
    {
        return this.sanctuary.getChunks();
    }

    public Map<ChunkPos, GrovesPOI.ClientGroveSanctuary.ChunkData> getChunkMap()
    {
        return this.chunks;
    }

    public void addChunk(ChunkPos pos)
    {
        GrovesPOI.ClientGroveSanctuary.ChunkData data = new GrovesPOI.ClientGroveSanctuary.ChunkData(pos, false);

        if (this.sanctuary.addChunk(data))
            putChunk(data);
    }

    public void putChunk(GrovesPOI.ClientGroveSanctuary.ChunkData chunk)
    {
        this.chunks.put(chunk.pos(), chunk);
    }

    public void setBuyChunkReason(Text reason)
    {
        this.buyChunkReason = reason.copy();
        this.buyChunkTicks = 100;   // 5 seconds;
    }

    public Text getBuyChunkReason() { return this.buyChunkReason; }

    public int getBuyChunkTicks() {
        int ticks = this.buyChunkTicks--;
        if (this.buyChunkTicks < 0) this.buyChunkTicks = 0;
        return ticks;
    }

    //
    // -- Friends

    //
    // -- Abiliies

    //
    // -- Keybinds (All client side processing)



    public boolean isSelectedTab(ScreenTab tab) { return this.currentTab == tab; }

    public ScreenTab getCurrentTab() { return this.currentTab; }

    public void setCurrentTab(ScreenTab tab)
    {
        this.currentTab = tab;
        for(Slot slot : this.slots)
        {
            if (slot instanceof TabSlot tabbed)
            {
                tabbed.setVisible(tab);
            }
        }
    }

    public static class TabSlot extends Slot implements ISlotVisibility {
        private final ScreenTab tab;
        private boolean visible;

        public TabSlot(ScreenTab tab, Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);

            this.tab = tab;
            this.visible = true;
        }

        public ScreenTab getTab() { return this.tab; }

        @Override
        public boolean isVisible() { return this.visible; }

        @Override
        public void setVisible(boolean visible) { this.visible = visible; }

        public void setVisible(ScreenTab tab)
        {
            this.visible = (this.tab == tab);
        }

        @Override
        public boolean isEnabled() {
            return this.visible && super.isEnabled();
        }
    }
}
