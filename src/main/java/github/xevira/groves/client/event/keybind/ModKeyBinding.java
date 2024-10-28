package github.xevira.groves.client.event.keybind;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public abstract class ModKeyBinding extends KeyBinding {
    public ModKeyBinding(String translationKey, int code, String category) {
        this(translationKey, InputUtil.Type.KEYSYM, code, category);
    }

    public ModKeyBinding(String translationKey, InputUtil.Type type, int code, String category)
    {
        super(translationKey,type, code, category);
    }

    public abstract void onPressed(MinecraftClient client, ClientPlayerEntity player);
}
