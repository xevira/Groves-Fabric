package github.xevira.groves.sanctuary.unlock;

import github.xevira.groves.Registration;
import github.xevira.groves.sanctuary.GroveSanctuary;
import github.xevira.groves.sanctuary.GroveUnlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class MoonwellUnlock extends GroveUnlock {
    public MoonwellUnlock() {
        super("moonwell", false);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Registration.MOON_PHIAL_ITEM);
    }

    @Override
    public String getEnglishToastTitle() {
        return "Moonwell Unlocked";
    }

    @Override
    public String getEnglishToastText() {
        return "Grow your sanctuary to four chunks.";
    }

    @Override
    public boolean checkForUnlock(MinecraftServer server, GroveSanctuary sanctuary, ServerPlayerEntity player) {
        return (sanctuary.totalChunks() >= 4);
    }
}
