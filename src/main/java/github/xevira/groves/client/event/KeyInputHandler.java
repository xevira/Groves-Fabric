package github.xevira.groves.client.event;

import github.xevira.groves.Groves;
import github.xevira.groves.client.event.keybind.GroveAbilityKeyBind;
import github.xevira.groves.client.event.keybind.ModKeyBinding;
import github.xevira.groves.client.event.keybind.OpenGrovesUIKeyBind;
import github.xevira.groves.sanctuary.GroveAbilities;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.codec.PacketCodec;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class KeyInputHandler {
    public static final String KEY_CATEGORY_GROVES = Groves.textPath("key.category", "groves");
    public static final String KEY_OPEN_GROVES_UI = Groves.textPath("key", "open_groves_ui");

    public static final ModKeyBinding OPEN_GROVE_UI_KEY = register(new OpenGrovesUIKeyBind(KEY_OPEN_GROVES_UI, InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, KEY_CATEGORY_GROVES));
    public static final List<ModKeyBinding> GROVE_ABILITY_KEYS = new ArrayList<>();

    // Used to keep from opening the Groves UI when doing an ability
    private static boolean didAbility = false;

    public static <T extends ModKeyBinding> ModKeyBinding register(T binding)
    {
        KeyBindingHelper.registerKeyBinding(binding);

        return binding;
    }

    private static void handleGrovesKeybinds(MinecraftClient client)
    {
        // Groves Key + ability key
        if (OPEN_GROVE_UI_KEY.isPressed())
        {
            // Groves functionality
            for(ModKeyBinding binding : GROVE_ABILITY_KEYS)
            {
                if (binding.wasPressed()) {
                    binding.onPressed(client, client.player);
                    didAbility = true;
                    break;
                }
            }
        }
        else if (OPEN_GROVE_UI_KEY.wasPressed())
        {
            if (!didAbility)
                OPEN_GROVE_UI_KEY.onPressed(client, client.player);

            didAbility = false;
        }

    }

    public static void load() {
        GroveAbilities.ABILITIES.forEach((id, ability) -> {
            ModKeyBinding binding = new GroveAbilityKeyBind(Groves.textPath("key", "groves_ability_" + ability.getName()), InputUtil.Type.KEYSYM, id, KEY_CATEGORY_GROVES);

            register(binding);

            GROVE_ABILITY_KEYS.add(binding);
        });

        ClientTickEvents.END_CLIENT_TICK.register(KeyInputHandler::handleGrovesKeybinds);


    }
}
