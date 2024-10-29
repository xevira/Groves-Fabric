package github.xevira.groves.client.event.keybind;

import github.xevira.groves.network.GroveAbitlityKeybindPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;

public class GroveAbilityKeyBind extends ModKeyBinding {
    private final int id;

    public GroveAbilityKeyBind(String translationKey, int id, String category) {
        super(translationKey, InputUtil.UNKNOWN_KEY.getCode(), category);

        this.id = id;
    }

    public GroveAbilityKeyBind(String translationKey, InputUtil.Type type, int id, String category) {
        super(translationKey, type, InputUtil.UNKNOWN_KEY.getCode(), category);

        this.id = id;
    }

    @Override
    public void onPressed(MinecraftClient client, ClientPlayerEntity player) {
        ClientPlayNetworking.send(new GroveAbitlityKeybindPayload(this.id));
    }
}
