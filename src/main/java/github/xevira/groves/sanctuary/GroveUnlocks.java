package github.xevira.groves.sanctuary;

import github.xevira.groves.sanctuary.unlock.MoonwellUnlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;

public class GroveUnlocks {
    public static final Map<String, GroveUnlock> UNLOCK_MAP = new HashMap<>();

    public static final GroveUnlock MOONWELL = new MoonwellUnlock();

    private static <T extends GroveUnlock> void registerUnlock(T unlock)
    {
        UNLOCK_MAP.put(unlock.getName(), unlock);
    }

    public static GroveUnlock getByName(String name)
    {
        return UNLOCK_MAP.values().stream().filter(unlock -> unlock.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    // Called from the sanctuary's server tick handler
    public static void checkUnlocks(MinecraftServer server, GroveSanctuary sanctuary, ServerPlayerEntity player)
    {
        UNLOCK_MAP.values().forEach(unlock -> unlock.checkUnlock(server, sanctuary, player));
    }

    public static void register()
    {
        registerUnlock(MOONWELL);
    }

    @Environment(EnvType.CLIENT)
    public static void toast(String name)
    {
        GroveUnlock unlock = getByName(name);

        if (unlock != null)
        {
            MinecraftClient.getInstance().getToastManager().add(new GroveUnlockToast(unlock));
        }
    }
}
