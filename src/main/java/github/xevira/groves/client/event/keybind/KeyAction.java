package github.xevira.groves.client.event.keybind;

import github.xevira.groves.Groves;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Arrays;

public enum KeyAction {
    PRESS("press", Groves.textPath("keyaction", "press")),
    RELEASE("release", Groves.textPath("keyaction", "release")),
    BOTH("both", Groves.textPath("keyaction", "both"))
    ;


    private final String name;
    private final String translationKey;

    KeyAction(String name, String translationKey)
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

    public KeyAction next()
    {
        int id = this.ordinal() + 1;
        if (id >= values().length)
        {
            id = 0;
        }

        return values()[id % values().length];
    }

    public KeyAction previous()
    {
        int id = this.ordinal() - 1;
        if (id < 0)
        {
            id = values().length - 1;
        }

        return values()[id % values().length];
    }

    public static KeyAction fromString(String name)
    {
        return Arrays.stream(KeyAction.values()).filter(action -> action.getName().equalsIgnoreCase(name)).findFirst().orElse(KeyAction.PRESS);
    }
}
