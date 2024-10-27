package github.xevira.groves.events;

import github.xevira.groves.item.MoonPhialItem;
import github.xevira.groves.poi.GrovesPOI;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class ModServerTickEvents {

    public static void onEndServerTick(MinecraftServer server)
    {
        GrovesPOI.onEndServerTick(server);


    }

    public static void onStartWorldTick(World world)
    {
        if (world.getRegistryKey().getValue().equals(World.OVERWORLD.getValue()))
        {
            // Overworld
            MoonPhialItem.updateLunarPhase(world);
        }
    }
}
