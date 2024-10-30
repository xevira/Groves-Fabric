package github.xevira.groves.sanctuary;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import github.xevira.groves.Groves;
import github.xevira.groves.poi.GrovesPOI;
import github.xevira.groves.sanctuary.ability.ChunkLoadAbility;
import github.xevira.groves.util.JSONHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class GroveAbility {
    public static final PacketCodec<RegistryByteBuf, GroveAbility> PACKET_CODEC = new PacketCodec<RegistryByteBuf, GroveAbility>() {
        @Override
        public GroveAbility decode(RegistryByteBuf buf) {
            String name = PacketCodecs.STRING.decode(buf);
            boolean active = PacketCodecs.BOOL.decode(buf);

            Optional<GroveAbility> template = GroveAbilities.getByName(name);
            if (template.isPresent())
            {
                GroveAbility ability = template.get().getConstructor().get();
                ability.active = active;
                return ability;
            }

            return null;
        }

        @Override
        public void encode(RegistryByteBuf buf, GroveAbility value) {
            PacketCodecs.STRING.encode(buf, value.getName());
            PacketCodecs.BOOL.encode(buf, value.isActive());
        }
    };

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

    public long startCost() { return 0L; }

    public long tickCost() { return 0L; }

    public  long useCost() { return 0L; }

    /** Whether to automatically deactivate if the {@code onServerTick} returns {@code true} **/
    public boolean autoDeactivate() { return this.autoDeactivate; }

    public boolean isActive() { return this.active; }

    public void setActive(boolean value) { this.active = value; }

    /** Determines whether the sanctuary can enable the ability **/
    public boolean canActivate(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player)
    {
        return false;
    }

    public abstract void sendFailure(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player);

    /** Action on performed when the ability is turned on **/
    public void onActivate(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player)
    {
    }

    /** Action on performed when the ability is turned off **/
    public void onDeactivate(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player)
    {
    }

    /**
     *  Action performed when the sanctuary handles its server tick
     *
     *  {@return whether to disable the ability}
     ***/
    public boolean onServerTick(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary)
    {
        return true;
    }

    public boolean canUse(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player)
    {
        return false;
    }

    public boolean onUse(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player)
    {
        return true;
    }

    public JsonObject serialize()
    {
        JsonObject json = new JsonObject();

        json.add("name", new JsonPrimitive(this.name));
        json.add("active", new JsonPrimitive(this.active));

        return json;
    }

    public static Optional<GroveAbility> deserialize(JsonObject json)
    {
        String name = JSONHelper.getString(json, "name");
        Optional<Boolean> active = JSONHelper.getBoolean(json, "active");

        if (name != null) {
            Optional<GroveAbility> ability = GroveAbilities.getByName(name);

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
