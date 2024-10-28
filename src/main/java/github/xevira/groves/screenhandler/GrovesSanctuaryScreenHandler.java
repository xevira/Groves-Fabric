package github.xevira.groves.screenhandler;

import github.xevira.groves.Registration;
import github.xevira.groves.block.entity.MoonwellMultiblockMasterBlockEntity;
import github.xevira.groves.network.GrovesSanctuaryScreenPayload;
import github.xevira.groves.network.MoonwellScreenPayload;
import github.xevira.groves.poi.GrovesPOI;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import org.jetbrains.annotations.Nullable;

public class GrovesSanctuaryScreenHandler extends ScreenHandler {
    private final GrovesPOI.ClientGroveSanctuary sanctuary;

    @SuppressWarnings("DataFlowIssue")
    public GrovesSanctuaryScreenHandler(int syncId, PlayerInventory playerInventory, GrovesSanctuaryScreenPayload payload)
    {
        this(syncId, playerInventory, payload.sanctuary());
    }

    public GrovesSanctuaryScreenHandler(int syncId, PlayerInventory playerInventory, GrovesPOI.ClientGroveSanctuary sanctuary)
    {
        super(Registration.GROVES_SANCTUARY_SCREEN_HANDLER, syncId);

        this.sanctuary = sanctuary;
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

    // TODO: Add update methods for the screen
}
