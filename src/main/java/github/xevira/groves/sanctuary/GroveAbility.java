package github.xevira.groves.sanctuary;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import github.xevira.groves.Groves;
import github.xevira.groves.poi.GrovesPOI;
import github.xevira.groves.sanctuary.ability.ChunkLoadAbility;
import github.xevira.groves.util.JSONHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class GroveAbility {
    private static int NextId = 0;

    protected final int id;
    protected final String name;
    protected final boolean automatic;
    protected final boolean autoDeactivate;
    protected final boolean defaultAllow;

    private boolean active;

    public GroveAbility(final String name, final boolean automatic, final boolean autodeactivate, final boolean defaultAllow)
    {
        this.id = ++NextId;
        this.name = name;
        this.automatic = automatic;
        this.autoDeactivate = autodeactivate;
        this.defaultAllow = defaultAllow;

        this.active = false;
    }

    public abstract Supplier<? extends GroveAbility> getConstructor();

    public boolean isAutomatic()
    {
        return this.automatic;
    }

    public int getId()
    {
        return this.id;
    }

    public boolean getDefaultAllow()
    {
        return this.defaultAllow;
    }

    public String getName()
    {
        return this.name;
    }

    /** Indicates whether the mod has enabled this ability **/
    public boolean isEnabled()
    {
        // TODO: add feature set check
        return true;
    }

    public abstract long startCost();

    public abstract long tickCost();

    public abstract long useCost();

    /** Whether to automatically deactivate if the {@code onServerTick} returns {@code true} **/
    public boolean autoDeactivate() { return this.autoDeactivate; }

    public boolean isActive() { return this.active; }

    public void setActive(boolean value) { this.active = value; }

    /** Determines whether the sanctuary can enable the ability **/
    public abstract boolean canActivate(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player);

    public abstract void sendFailure(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player);

    /** Action on performed when the ability is turned on **/
    public abstract void onActivate(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player);

    /** Action on performed when the ability is turned off **/
    public abstract void onDeactivate(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player);

    /**
     *  Action performed when the sanctuary handles its server tick
     *
     *  {@return whether to disable the ability}
     ***/
    public abstract boolean onServerTick(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary);

    public abstract boolean canUse(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player);

    public abstract boolean onUse(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player);

    public JsonObject serialize()
    {
        JsonObject json = new JsonObject();

        json.add("id", new JsonPrimitive(this.id));
        json.add("active", new JsonPrimitive(this.active));

        return json;
    }

    public static Optional<GroveAbility> deserialize(JsonObject json)
    {
        Optional<Integer> id = JSONHelper.getInt(json, "id");
        Optional<Boolean> active = JSONHelper.getBoolean(json, "active");

        if (id.isPresent()) {
            Optional<GroveAbility> ability = GroveAbilities.getById(id.get());

            if (ability.isPresent()) {
                ability.get().active = active.isPresent() && active.get();

                return ability;
            }
        }

        return Optional.empty();
    }

    public static abstract class AutomaticGroveAbility extends GroveAbility {
        public AutomaticGroveAbility(String name, boolean autodeactivate, boolean defaultAllow) {
            super(name, true, autodeactivate, defaultAllow);
        }
    }

    public static abstract class ManualGroveAbility extends GroveAbility {
        public ManualGroveAbility(String name, boolean defaultAllow) {
            super(name, false, false, defaultAllow);
        }
    }
}
