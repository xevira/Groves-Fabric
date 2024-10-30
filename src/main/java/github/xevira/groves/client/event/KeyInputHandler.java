package github.xevira.groves.client.event;

import com.google.gson.JsonObject;
import github.xevira.groves.Groves;
import github.xevira.groves.client.event.keybind.*;
import github.xevira.groves.network.GroveAbitlityKeybindPayload;
import github.xevira.groves.network.OpenGrovesRequestPayload;
import github.xevira.groves.util.JSONHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KeyInputHandler {
    public static final String KEY_CATEGORY_GROVES = Groves.textPath("key.category", "groves");
    public static final String KEY_OPEN_GROVES_UI = Groves.textPath("key", "open_groves_ui");

    public static final List<HotKey> HOT_KEYS = new ArrayList<>();

    public static final HotKey OPEN_GROVES_UI_KEY = new HotKey("open_groves_ui", HotKeyType.GENERAL, "G", KeybindSettings.RELEASE_EXCLUSIVE, "Opens the Groves UI")
            .setCallback((action, key) -> {
                ClientPlayNetworking.send(new OpenGrovesRequestPayload());
                return true;
            });
    public static final HotKey CHUNK_LOAD_ABILITY = new HotKey("chunk_load", HotKeyType.ABILITY, "G,C");
    public static final HotKey REGENERATION_ABILITY = new HotKey("regeneration", HotKeyType.ABILITY, "G,R");

    public static void registerHotKey(HotKey key)
    {
        HOT_KEYS.add(key);
    }

    public static @Nullable HotKey getHotKey(String name)
    {
        return HOT_KEYS.stream().filter(hk -> hk.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static void load() {
        // Set the ABILITY hotkey callback
        HOT_KEYS.stream().filter(hotKey -> hotKey.getType() == HotKeyType.ABILITY).forEach(hotkey -> {
            hotkey.getKeybind().setCallback((action, key) -> {
                ClientPlayNetworking.send(new GroveAbitlityKeybindPayload(hotkey.getName()));
                return true;
            });
        });


        // Standard keybind
//        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
//            if (OPEN_GROVE_UI_KEY.wasPressed())
//            {
//                ClientPlayNetworking.send(new OpenGrovesRequestPayload());
//            }
//        });
    }

    private static boolean checkKeyBindsForChanges(int keyCode)
    {
        boolean cancel = false;

        if (!HOT_KEYS.isEmpty())
        {
            for (HotKey key : HOT_KEYS)
            {
                //Groves.LOGGER.info("Checking hotkey {},", key.getName());
                // Note: isPressed() has to get called for key releases too, to reset the state
                cancel |= key.getKeybind().updateIsPressed();
            }
        }

        return cancel;
    }

    public static boolean onKeyInput(int keyCode, int scanCode, int modifiers, int action, @NotNull MinecraftClient mc)
    {
        // Update the pressed key states
        Keybind.onKeyInputPre(keyCode, scanCode, modifiers, action);

        return checkKeyBindsForChanges(keyCode);
    }

    public static void loadConfig(JsonObject root)
    {
        Optional<JsonObject> obj = JSONHelper.getObject(root, "hotkeys");

        if (obj.isPresent())
        {
            for(HotKey hk : HOT_KEYS)
            {
                Optional<JsonObject> hkJson = JSONHelper.getObject(obj.get(), hk.getName());

                hkJson.ifPresent(jsonObject -> hk.getKeybind().setValueFromJsonElement(jsonObject));
            }
        }
    }

    public static void saveConfig(JsonObject root)
    {
        JsonObject obj = new JsonObject();

        for(HotKey hk : HOT_KEYS)
        {
            obj.add(hk.getName(), hk.getKeybind().getAsJsonElement());
        }

        root.add("hotkeys", obj);
    }

    public static void updateKeyBinds()
    {

    }
}
