package github.xevira.groves.mixin.client;

import github.xevira.groves.ClientConfig;
import github.xevira.groves.client.event.KeyInputHandler;
import github.xevira.groves.client.event.keybind.Keybind;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    public ClientWorld world;

    @Unique
    private ClientWorld worldBefore;

    private static void postWorldOperation(ClientWorld worldBefore, ClientWorld worldAfter)
    {
        // Save all the configs when exiting a world
        if (worldBefore != null && worldAfter == null) {
            ClientConfig.save();
        }

        // (Re-)Load all the configs from file when entering a world
        else if (worldBefore == null && worldAfter != null) {
            ClientConfig.load();
            // TODO: Update keys?
        }
    }


    @Inject(method = "<init>(Lnet/minecraft/client/RunArgs;)V", at = @At("RETURN"))
    private void onInitComplete(RunArgs args, CallbackInfo ci)
    {
        ClientConfig.load();
    }

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void onPostKeyboardInput(CallbackInfo ci)
    {
        Keybind.reCheckPressedKeys();
    }

    @Inject(method = "joinWorld(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/gui/screen/DownloadingTerrainScreen$WorldEntryReason;)V", at = @At("HEAD"))
    private void onLoadWorldPre(ClientWorld worldClientIn, DownloadingTerrainScreen.WorldEntryReason worldEntryReason, CallbackInfo ci)
    {
        this.worldBefore = this.world;
    }

    @Inject(method = "joinWorld(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/gui/screen/DownloadingTerrainScreen$WorldEntryReason;)V", at = @At("RETURN"))
    private void onLoadWorldPost(ClientWorld worldClientIn, DownloadingTerrainScreen.WorldEntryReason worldEntryReason, CallbackInfo ci)
    {
        postWorldOperation(this.worldBefore, worldClientIn);

        this.worldBefore = null;
    }

    @Inject(method = "enterReconfiguration(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
    private void onReconfigurationPre(Screen screen, CallbackInfo ci)
    {
        this.worldBefore = this.world;
    }

    @Inject(method = "enterReconfiguration(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("RETURN"))
    private void onReconfigurationPost(Screen screen, CallbackInfo ci)
    {
        postWorldOperation(this.worldBefore, null);
        this.worldBefore = null;
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V", at = @At("HEAD"))
    private void onDisconnectPre(Screen screen, boolean bl, CallbackInfo ci)
    {
        this.worldBefore = this.world;
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V", at = @At("RETURN"))
    private void onDisconnectPost(Screen screen, boolean bl, CallbackInfo ci)
    {
        postWorldOperation(this.worldBefore, null);
        this.worldBefore = null;
    }
}
