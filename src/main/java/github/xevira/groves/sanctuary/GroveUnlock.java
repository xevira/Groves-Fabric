package github.xevira.groves.sanctuary;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import github.xevira.groves.Groves;
import github.xevira.groves.network.GroveUnlockToastPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;

public abstract class GroveUnlock {
    // Properties
    private final String name;
    private final boolean challenge;

    public GroveUnlock(String name, boolean challenge)
    {
        this.name = name;
        this.challenge = challenge;
    }

    public String getName()
    {
        return this.name;
    }

    public boolean isChallenge()
    {
        return this.challenge;
    }

    public MutableText getToastTitle()
    {
        return Groves.text("toast", this.name + ".title");
    }

    public abstract String getEnglishToastTitle();

    public MutableText getToastText()
    {
        return Groves.text("toast", this.name + ".text");
    }

    public abstract String getEnglishToastText();


    public ItemStack getIcon()
    {
        return null;
    }

    public Identifier getIconTexture()
    {
        return null;
    }

    public abstract boolean checkForUnlock(MinecraftServer server, GroveSanctuary sanctuary, ServerPlayerEntity player);

    public void checkUnlock(MinecraftServer server, GroveSanctuary sanctuary, ServerPlayerEntity player)
    {
        if (!sanctuary.hasUnlock(this) && this.checkForUnlock(server, sanctuary, player)) {
            sanctuary.grantUnlock(this);
            ServerPlayNetworking.send(player, new GroveUnlockToastPayload(this.name));
        }
    }
}
