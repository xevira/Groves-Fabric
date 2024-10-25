package github.xevira.groves.util;

public enum LunarPhasesEnum {
    FULL_MOON("full_moon"),
    WANING_GIBBOUS("waning_gibbous"),
    THIRD_QUARTER("third_quarter"),
    WANING_CRESCENT("waning_crescent"),
    NEW_MOON("new_moon"),
    WAXING_CRESCENT("waxing_crescent"),
    FIRST_QUARTER("first_quarter"),
    WAXING_GIBBOUS("waxing_gibbous");

    private final String name;

    LunarPhasesEnum(String name)
    {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
