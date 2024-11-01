package github.xevira.groves.util;

import net.minecraft.util.StringIdentifiable;

public enum ScreenTab implements StringIdentifiable {
    GENERAL("general"),
    CHUNKS("chunks"),
    FRIENDS("friends"),
    ABILITIES("abilities"),
    KEYBINDS("keybinds");

    private final String name;

    ScreenTab(String name) {
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }

    @Override
    public String asString() {
        return this.name;
    }
}
