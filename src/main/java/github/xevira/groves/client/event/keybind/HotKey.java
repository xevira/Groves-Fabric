package github.xevira.groves.client.event.keybind;

import com.google.gson.JsonElement;
import github.xevira.groves.Groves;
import github.xevira.groves.client.event.KeyInputHandler;

public class HotKey {

    private final String name;
    private final HotKeyType type;
    private String comment;
    private Keybind keybind;

    public HotKey(String name, HotKeyType type, String defaultKey)
    {
        this(name, type, defaultKey, name + " comment");
    }

    public HotKey(String name, HotKeyType type, String defaultKey, String comment)
    {
        this(name, type, defaultKey, KeybindSettings.DEFAULT, comment);
    }


    public HotKey(String name, HotKeyType type, String defaultKey, KeybindSettings settings, String comment)
    {
        this.name = name;
        this.type = type;
        this.comment = comment;
        this.keybind = Keybind.fromStorageString(defaultKey, settings);

        KeyInputHandler.registerHotKey(this);
    }

    public String getName()
    {
        return this.name;
    }

    public HotKeyType getType() { return this.type; }

    public Keybind getKeybind()
    {
        return this.keybind;
    }

    public String getStringValue()
    {
        return this.keybind.getStringValue();
    }

    public void setValueFromString(String value)
    {
        this.keybind.setValueFromString(value);
    }

    public boolean isModified()
    {
        return this.keybind.isModified();
    }

    public boolean isModified(String newValue)
    {
        return this.keybind.isModified(newValue);
    }

    public void resetToDefault()
    {
        this.keybind.resetToDefault();
    }

    public HotKey setCallback(IKeybindCallback callback)
    {
        this.keybind.setCallback(callback);
        return this;
    }

    public void setValueFromJsonElement(JsonElement element)
    {
        try
        {
            if (element.isJsonObject())
            {
                this.keybind.setValueFromJsonElement(element);
            }
            else
            {
                Groves.LOGGER.warn("Failed to set config value for '{}' from the JSON element '{}'", this.getName(), element);
            }
        }
        catch (Exception e)
        {
            Groves.LOGGER.warn("Failed to set config value for '{}' from the JSON element '{}'", this.getName(), element, e);
        }
    }

}
