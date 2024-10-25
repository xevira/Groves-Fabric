package github.xevira.groves.events;

import github.xevira.groves.item.MoonPhialItem;
import net.minecraft.world.World;

public class ModServerTickEvents {

    public static void onStartTick(World world)
    {
        if (world.getRegistryKey().getValue().equals(World.OVERWORLD.getValue()))
        {
            // Overworld
            MoonPhialItem.updateLunarPhase(world);
        }
    }
}
