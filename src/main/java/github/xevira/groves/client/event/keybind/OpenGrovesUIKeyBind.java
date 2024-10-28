package github.xevira.groves.client.event.keybind;

import github.xevira.groves.network.OpenGrovesRequestPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

public class OpenGrovesUIKeyBind extends ModKeyBinding {
    public OpenGrovesUIKeyBind(String translationKey, int code, String category) {
        super(translationKey, code, category);
    }

    public OpenGrovesUIKeyBind(String translationKey, InputUtil.Type type, int code, String category) {
        super(translationKey, type, code, category);
    }

    @Override
    public void onPressed(MinecraftClient client, ClientPlayerEntity player) {
        ClientPlayNetworking.send(new OpenGrovesRequestPayload());
    }
}
