package github.xevira.groves.client.event.keybind;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import github.xevira.groves.Groves;
import github.xevira.groves.util.JSONHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class Keybind {
    private static final ArrayList<Integer> PRESSED_KEYS = new ArrayList<>();
    private static int maxKeyCount = 0;
    private static int triggeredCount;

    private final String defaultStorageString;
    private final KeybindSettings defaultSettings;
    private List<Integer> keyCodes = new ArrayList<>(4);
    private KeybindSettings settings;
    private boolean pressed;
    private boolean pressedLast;
    private int heldTime;

    @Nullable
    private IKeybindCallback callback;

    private Keybind(String defaultStorageString, KeybindSettings settings)
    {
        this.defaultStorageString = defaultStorageString;
        this.defaultSettings = settings;
        this.settings = settings;
    }

    public KeybindSettings getSettings()
    {
        return this.settings;
    }

    public void setSettings(KeybindSettings settings)
    {
        this.settings = settings;
    }

    public void setCallback(@Nullable IKeybindCallback callback)
    {
        this.callback = callback;
    }

    public boolean isValid()
    {
        return !this.keyCodes.isEmpty() || this.settings.getAllowEmpty();
    }

    public boolean isPressed()
    {
        return this.pressed && !this.pressedLast && this.heldTime == 0;
    }

    public boolean isHeld()
    {
        return this.pressed || (this.settings.getAllowEmpty() && this.keyCodes.isEmpty());
    }

    public boolean updateIsPressed()
    {
        if (this.keyCodes.isEmpty() ||
                (this.settings.getContext() != KeybindSettings.Context.ANY &&
                        ((this.settings.getContext() == KeybindSettings.Context.INGAME) != (MinecraftClient.getInstance().currentScreen == null))))
        {
            this.pressed = false;
            return false;
        }

        boolean allowExtraKeys = this.settings.getAllowExtraKeys();
        boolean allowOutOfOrder = !this.settings.isOrderSensitive();
        boolean pressedLast = this.pressed;
        final int sizePressed = PRESSED_KEYS.size();
        final int sizeRequired = this.keyCodes.size();

        if (sizePressed >= sizeRequired && (allowExtraKeys || sizePressed == sizeRequired))
        {
            int keyCodeIndex = 0;
            this.pressed = PRESSED_KEYS.containsAll(this.keyCodes);

            for (int i = 0; i < sizePressed; ++i)
            {
                Integer keyCodeObj = PRESSED_KEYS.get(i);

                if (this.keyCodes.get(keyCodeIndex).equals(keyCodeObj))
                {
                    if (++keyCodeIndex >= sizeRequired)
                    {
                        break;
                    }
                }
                else if ((!allowOutOfOrder && (keyCodeIndex > 0 || sizePressed == sizeRequired)) ||
                        (!this.keyCodes.contains(keyCodeObj) && !allowExtraKeys))
                {
                    this.pressed = false;
                    break;
                }
            }
        }
        else
        {
            this.pressed = false;
        }

        KeyAction activateOn = this.settings.getActivateOn();

        if (this.pressed != pressedLast &&
                (allowExtraKeys || sizeRequired == maxKeyCount) &&
                (triggeredCount == 0 || !this.settings.isExclusive()) &&
                (activateOn == KeyAction.BOTH || this.pressed == (activateOn == KeyAction.PRESS)))
        {
            boolean cancel = this.triggerKeyAction(pressedLast) && this.settings.shouldCancel();
            //System.out.printf("triggered, cancel: %s, triggeredCount: %d\n", cancel, triggeredCount);

            if (cancel)
            {
                ++triggeredCount;
            }

            return cancel;
        }

        return false;
    }

    private boolean triggerKeyAction(boolean pressedLast)
    {
        boolean cancel = false;

        if (!this.pressed)
        {
            this.heldTime = 0;
            KeyAction activateOn = this.settings.getActivateOn();

            if (pressedLast && this.callback != null && (activateOn == KeyAction.RELEASE || activateOn == KeyAction.BOTH))
            {
                cancel = this.callback.onKeyAction(KeyAction.RELEASE, this);
            }
        }
        else if (!pressedLast && this.heldTime == 0)
        {
            if (this.keyCodes.contains(KeyCodes.KEY_F3))
            {
                // Prevent the debug GUI from opening after the F3 key is released
                ((IF3KeyStateSetter) MinecraftClient.getInstance().keyboard).grovesSetF3KeyState(true);
            }

            KeyAction activateOn = this.settings.getActivateOn();

            if (this.callback != null && (activateOn == KeyAction.PRESS || activateOn == KeyAction.BOTH))
            {
                cancel = this.callback.onKeyAction(KeyAction.PRESS, this);
            }
        }

        return cancel;
    }

    public void clearKeys()
    {
        this.keyCodes.clear();
        this.pressed = false;
        this.heldTime = 0;
    }

    public void addKey(int keyCode)
    {
        if (!this.keyCodes.contains(keyCode))
            this.keyCodes.add(keyCode);
    }

    public void removeKey(int keyCode)
    {
        this.keyCodes.remove(keyCode);
    }

    public void tick()
    {
        if (this.pressed)
        {
            this.heldTime++;
        }

        this.pressedLast = this.pressed;
    }

    public List<Integer> getKeys()
    {
        return this.keyCodes;
    }

    public String getKeysDisplayString()
    {
        return this.getStringValue().replaceAll(",", " + ");
    }

    public boolean isModified()
    {
        return !this.getStringValue().equals(this.defaultStorageString);
    }

    public boolean isModified(String newValue)
    {
        return !this.defaultStorageString.equals(newValue);
    }

    public void resetToDefault()
    {
        this.setValueFromString(this.defaultStorageString);
    }

    public boolean areSettingsModified()
    {
        return !this.settings.equals(this.defaultSettings);
    }

    public void resetSettingsToDefaults()
    {
        this.settings = this.defaultSettings;
    }

    public String getStringValue()
    {
        StringBuilder sb = new StringBuilder(32);

        for (int i = 0; i < this.keyCodes.size(); ++i)
        {
            if (i > 0)
            {
                sb.append(",");
            }

            int keyCode = this.keyCodes.get(i);
            String name = getStorageStringForKeyCode(keyCode);

            if (name != null)
            {
                sb.append(name);
            }
        }

        return sb.toString();
    }

    public String getDefaultStringValue()
    {
        return this.defaultStorageString;
    }

    public void setValueFromString(String str)
    {
        this.clearKeys();
        String[] keys = str.split(",");

        for (String keyName : keys)
        {
            keyName = keyName.trim();

            if (!keyName.isEmpty())
            {
                int keyCode = KeyCodes.getKeyCodeFromName(keyName);

                if (keyCode != KeyCodes.KEY_NONE)
                {
                    this.addKey(keyCode);
                }
            }
        }
    }

    public boolean matches(int keyCode)
    {
        return this.keyCodes.size() == 1 && this.keyCodes.get(0) == keyCode;
    }


    public static int getKeyCode(KeyBinding keybind)
    {
        InputUtil.Key input = InputUtil.fromTranslationKey(keybind.getBoundKeyTranslationKey());
        return input.getCategory() == InputUtil.Type.MOUSE ? input.getCode() - 100 : input.getCode();
    }

    public boolean overlaps(Keybind other)
    {
        if (other == this || other.getKeys().size() > this.getKeys().size())
        {
            return false;
        }

        if (this.contextOverlaps(other))
        {
            KeybindSettings settingsOther = other.getSettings();
            boolean o1 = this.settings.isOrderSensitive();
            boolean o2 = settingsOther.isOrderSensitive();
            List<Integer> keys1 = this.getKeys();
            List<Integer> keys2 = other.getKeys();
            int l1 = keys1.size();
            int l2 = keys2.size();

            if (l1 == 0 || l2 == 0)
            {
                return false;
            }

            if ((!this.settings.getAllowExtraKeys() && l1 < l2 && keys1.get(0) != keys2.get(0)) ||
                    (!settingsOther.getAllowExtraKeys() && l2 < l1 && keys1.get(0) != keys2.get(0)))
            {
                return false;
            }

            // Both are order sensitive, try to "slide the shorter sequence over the longer sequence" to find a match
            if (o1 && o2)
            {
                return l1 < l2 ? Collections.indexOfSubList(keys2, keys1) != -1 : Collections.indexOfSubList(keys1, keys2) != -1;
            }
            // At least one of the keybinds is not order sensitive
            else
            {
                return l1 <= l2 ? keys2.containsAll(keys1) : keys1.containsAll(keys2);
            }
        }

        return false;
    }


    public boolean contextOverlaps(Keybind other)
    {
        KeybindSettings settingsOther = other.getSettings();
        KeybindSettings.Context c1 = this.settings.getContext();
        KeybindSettings.Context c2 = settingsOther.getContext();

        if (c1 == KeybindSettings.Context.ANY || c2 == KeybindSettings.Context.ANY || c1 == c2)
        {
            KeyAction a1 = this.settings.getActivateOn();
            KeyAction a2 = settingsOther.getActivateOn();

            if (a1 == KeyAction.BOTH || a2 == KeyAction.BOTH || a1 == a2)
            {
                return true;
            }
        }

        return false;
    }

    public static Keybind fromStorageString(String str, KeybindSettings settings)
    {
        Keybind keybind = new Keybind(str, settings);
        keybind.setValueFromString(str);
        return keybind;
    }

    public static void onKeyInputPre(int keyCode, int scanCode, int modifiers, int action)
    {
        if (keyCode != -1)
        {
            Integer keyCodeInt = keyCode;
            boolean state = action != GLFW.GLFW_RELEASE;

            if (state)
            {
                if (!PRESSED_KEYS.contains(keyCodeInt))
                {
                    // TODO: Check for ignored keys
                    PRESSED_KEYS.add(keyCodeInt);
                    maxKeyCount = PRESSED_KEYS.size();
                }
            }
            else
            {
                PRESSED_KEYS.remove(keyCodeInt);
                // Keep the max key count the same upon release
            }
        }
    }


    public static boolean isKeyDown(int keyCode)
    {
        if (keyCode == -1)
        {
            return false;
        }

        long window = MinecraftClient.getInstance().getWindow().getHandle();

        if (keyCode >= 0)
        {
            return GLFW.glfwGetKey(window, keyCode) == GLFW.GLFW_PRESS;
        }

        keyCode += 100;

        return keyCode >= 0 && GLFW.glfwGetMouseButton(window, keyCode) == GLFW.GLFW_PRESS;
    }


    public static String getActiveKeysString()
    {
        if (!PRESSED_KEYS.isEmpty())
        {
            StringBuilder sb = new StringBuilder(128);
            int i = 0;

            for (int key : PRESSED_KEYS)
            {
                if (i > 0)
                {
                    sb.append(" + ");
                }

                String name = getStorageStringForKeyCode(key);

                if (name != null)
                {
                    sb.append(String.format("%s (%d)", name, key));
                }

                i++;
            }

            return sb.toString();
        }

        return "<none>";
    }

    public static void reCheckPressedKeys()
    {
        Iterator<Integer> iter = PRESSED_KEYS.iterator();

        while (iter.hasNext())
        {
            int keyCode = iter.next();

            if (!isKeyDown(keyCode))
            {
                iter.remove();
            }
        }

        // Clear the triggered count after all keys have been released
        if (PRESSED_KEYS.isEmpty())
        {
            triggeredCount = 0;
            maxKeyCount = 0;
        }
    }

    @Nullable
    public static String getStorageStringForKeyCode(int keyCode)
    {
        return KeyCodes.getNameForKey(keyCode);
    }

    public static int getTriggeredCount()
    {
        return triggeredCount;
    }

    public IKeybindCallback getCallback() {
        return callback;
    }

    public JsonElement getAsJsonElement()
    {
        JsonObject obj = new JsonObject();
        obj.add("keys", new JsonPrimitive(this.getStringValue()));

        if (this.areSettingsModified())
        {
            obj.add("settings", this.getSettings().toJson());
        }

        return obj;
    }

    public void setValueFromJsonElement(JsonElement element)
    {
        if (element.isJsonObject())
        {
            JsonObject obj = element.getAsJsonObject();

            String keys = JSONHelper.getString(obj, "keys");
            if (keys != null) this.setValueFromString(keys);

            Optional<JsonObject> settings = JSONHelper.getObject(obj, "settings");
            settings.ifPresent(jsonObject -> this.setSettings(KeybindSettings.fromJson(jsonObject)));
        }
    }
}
