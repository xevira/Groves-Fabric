package github.xevira.groves.client.event;

import github.xevira.groves.Groves;
import github.xevira.groves.client.event.keybind.ModKeyBinding;
import github.xevira.groves.client.event.keybind.OpenGrovesUIKeyBind;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class KeyInputHandler {
    private static final List<ModKeyBinding> MOD_KEYS = new ArrayList<>();

    public static final String KEY_CATEGORY_GROVES = Groves.textPath("key.category", "groves");
    public static final String KEY_OPEN_GROVES_UI = Groves.textPath("key", "open_groves_ui");

    public static final ModKeyBinding OPEN_GROVE_UI_KEY = register(new OpenGrovesUIKeyBind(KEY_OPEN_GROVES_UI, InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, KEY_CATEGORY_GROVES));

    public static <T extends ModKeyBinding> ModKeyBinding register(T binding)
    {
        KeyBindingHelper.registerKeyBinding(binding);

        MOD_KEYS.add(binding);

        return binding;
    }

    public static void load() {
        ClientTickEvents.END_CLIENT_TICK.register((client) ->
        {
            MOD_KEYS.stream()
                    .filter(KeyBinding::wasPressed)
                    .forEach(binding -> binding.onPressed(client, client.player));
        });
    }
}
