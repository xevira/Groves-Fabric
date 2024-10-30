package github.xevira.groves.client.event.keybind;

import com.google.gson.JsonObject;
import github.xevira.groves.util.JSONHelper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.Optional;

public class KeybindSettings
{
    public static final KeybindSettings DEFAULT                     = new KeybindSettings(Context.INGAME, KeyAction.PRESS, false, true, false, true);
    public static final KeybindSettings RELEASE_EXCLUSIVE           = new KeybindSettings(Context.INGAME, KeyAction.RELEASE, false, true, true, true);

    private final Context context;
    private final KeyAction activateOn;
    private final boolean allowEmpty;
    private final boolean allowExtraKeys;
    private final boolean orderSensitive;
    private final boolean exclusive;
    private final boolean cancel;

    private KeybindSettings(Context context, KeyAction activateOn, boolean allowExtraKeys, boolean orderSensitive, boolean exclusive, boolean cancel)
    {
        this(context, activateOn, allowExtraKeys, orderSensitive, exclusive, cancel, false);
    }

    private KeybindSettings(Context context, KeyAction activateOn, boolean allowExtraKeys, boolean orderSensitive, boolean exclusive, boolean cancel, boolean allowEmpty)
    {
        this.context = context;
        this.activateOn = activateOn;
        this.allowExtraKeys = allowExtraKeys;
        this.orderSensitive = orderSensitive;
        this.exclusive = exclusive;
        this.cancel = cancel;
        this.allowEmpty = allowEmpty;
    }

    public static KeybindSettings create(Context context, KeyAction activateOn, boolean allowExtraKeys, boolean orderSensitive, boolean exclusive, boolean cancel)
    {
        return create(context, activateOn, allowExtraKeys, orderSensitive, exclusive, cancel, false);
    }

    public static KeybindSettings create(Context context, KeyAction activateOn, boolean allowExtraKeys, boolean orderSensitive, boolean exclusive, boolean cancel, boolean allowEmpty)
    {
        return new KeybindSettings(context, activateOn, allowExtraKeys, orderSensitive, exclusive, cancel, allowEmpty);
    }

    public Context getContext() { return this.context; }

    public KeyAction getActivateOn() { return this.activateOn; }

    public boolean getAllowEmpty() { return this.allowEmpty; }

    public boolean getAllowExtraKeys() { return this.allowExtraKeys; }

    public boolean isOrderSensitive() { return this.orderSensitive; }

    public boolean isExclusive() { return this.exclusive; }

    public boolean shouldCancel() { return this.cancel; }

    public JsonObject toJson()
    {
        JsonObject obj = new JsonObject();

        obj.addProperty("activate_on", this.activateOn.name());
        obj.addProperty("context", this.context.name());
        obj.addProperty("allow_empty", this.allowEmpty);
        obj.addProperty("allow_extra_keys", this.allowExtraKeys);
        obj.addProperty("order_sensitive", this.orderSensitive);
        obj.addProperty("exclusive", this.exclusive);
        obj.addProperty("cancel", this.cancel);

        return obj;
    }

    public static KeybindSettings fromJson(JsonObject obj)
    {
        Context context = Context.INGAME;
        KeyAction activateOn = KeyAction.PRESS;
        String contextStr = JSONHelper.getString(obj, "context");
        String activateStr = JSONHelper.getString(obj, "activate_on");

        if (contextStr != null)
        {
            for (Context ctx : Context.values())
            {
                if (ctx.name().equalsIgnoreCase(contextStr))
                {
                    context = ctx;
                    break;
                }
            }
        }

        if (activateStr != null)
        {
            for (KeyAction act : KeyAction.values())
            {
                if (act.name().equalsIgnoreCase(activateStr))
                {
                    activateOn = act;
                    break;
                }
            }
        }

        boolean allowEmpty = JSONHelper.getBoolean(obj, "allow_empty", false);
        boolean allowExtraKeys = JSONHelper.getBoolean(obj, "allow_extra_keys", false);
        boolean orderSensitive = JSONHelper.getBoolean(obj, "order_sensitive", true);
        boolean exclusive = JSONHelper.getBoolean(obj, "exclusive", true);
        boolean cancel = JSONHelper.getBoolean(obj, "cancel", true);

        return create(context, activateOn, allowExtraKeys, orderSensitive, exclusive, cancel, allowEmpty);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        KeybindSettings other = (KeybindSettings) obj;
        if (activateOn != other.activateOn) return false;
        if (context != other.context) return false;
        if (allowEmpty != other.allowEmpty) return false;
        if (allowExtraKeys != other.allowExtraKeys) return false;
        if (cancel != other.cancel) return false;
        if (exclusive != other.exclusive) return false;
        if (orderSensitive != other.orderSensitive) return false;
        return true;
    }

    public enum Context
    {
        INGAME  ("ingame",  "malilib.label.key_context.ingame"),
        GUI     ("gui",     "malilib.label.key_context.gui"),
        ANY     ("any",     "malilib.label.key_context.any");

        private final String name;
        private final String translationKey;

        private Context(String name, String translationKey)
        {
            this.name = name;
            this.translationKey = translationKey;
        }

        public String getName()
        {
            return this.name;
        }

        public MutableText getText()
        {
            return Text.translatable(this.translationKey);
        }

        public Context next()
        {
            int id = this.ordinal() + 1;
            if (id >= values().length)
                id = 0;

            return values()[id % values().length];
        }

        public Context previous()
        {
            int id = this.ordinal() - 1;
            if (id < 0)
                id = values().length - 1;

            return values()[id % values().length];
        }

        public static Context fromString(String name)
        {
            return Arrays.stream(Context.values()).filter(context -> context.name.equalsIgnoreCase(name)).findFirst().orElse(Context.INGAME);
        }
    }
}