package github.xevira.groves.sanctuary;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import github.xevira.groves.Groves;
import github.xevira.groves.poi.GrovesPOI;
import github.xevira.groves.sanctuary.ability.ChunkLoadAbility;
import github.xevira.groves.util.JSONHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

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
            long cooldown = PacketCodecs.LONG.decode(buf);

            Optional<GroveAbility> template = GroveAbilities.getByName(name);
            if (template.isPresent())
            {
                GroveAbility ability = template.get().getConstructor().get();
                ability.active = active;
                ability.cooldownTicks = cooldown;
                return ability;
            }

            return null;
        }

        @Override
        public void encode(RegistryByteBuf buf, GroveAbility value) {
            PacketCodecs.STRING.encode(buf, value.getName());
            PacketCodecs.BOOL.encode(buf, value.isActive());
            PacketCodecs.LONG.encode(buf, value.cooldownTicks);
        }
    };

    public static final Codec<GroveAbility> CODEC = Codec.STRING.<GroveAbility>comapFlatMap(GroveAbility::validate, GroveAbility::getName).stable();

    private static int NextId = 0;

    protected final int id;
    protected final String name;
    protected final boolean automatic;
    protected final boolean autoDeactivate;
    protected final boolean defaultAllow;
    protected final boolean autoInstalled;
    protected final boolean forbidden;

    private boolean active;
    private long cooldownTicks;

    public GroveAbility(final String name, final boolean automatic, final boolean autoDeactivate, final boolean defaultAllow, final boolean autoInstalled, final boolean forbidden)
    {
        this.id = ++NextId;
        this.name = name;
        this.automatic = automatic;
        this.autoDeactivate = autoDeactivate;
        this.defaultAllow = defaultAllow;
        this.autoInstalled = autoInstalled;
        this.forbidden = forbidden;

        this.active = false;
        this.cooldownTicks = -1L;
    }

    public final boolean isAutoInstalled()
    {
        return this.autoInstalled;
    }

    public final boolean isForbidden()
    {
        return this.forbidden;
    }

    public abstract Supplier<? extends GroveAbility> getConstructor();

    public abstract @Nullable Item getRecipeIngredient();

    public abstract String getEnglishTranslation();

    public abstract String getEnglishLoreTranslation();

    public abstract String getEnglishStartCostTranslation();

    public abstract String getEnglishTickCostTranslation();

    public abstract String getEnglishUseCostTranslation();

    public @Nullable String getEnglishUnlockTranslation()
    {
        return null;
    }

    public boolean hasUnlockRequirement()
    {
        return false;
    }

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

    public boolean hasCooldown()
    {
        return this.cooldownTicks >= 0L;
    }

    public long getCooldown()
    {
        return this.cooldownTicks;
    }

    public void setCooldown(long ticks)
    {
        this.cooldownTicks = ticks;
    }

    /** Indicates whether the mod has enabled this ability **/
    public boolean isEnabled()
    {
        // TODO: add feature set check
        return true;
    }

    public long startCost() { return 0L; }

    public long tickCost() { return 0L; }

    public long useCost() { return 0L; }

    // Returns the reason why you can't unlock it.
    public @Nullable Text canUnlock(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player)
    {
        return null;
    }

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

    public final void activate(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player)
    {
        this.onActivate(server, sanctuary, player);
    }

    /** Action on performed when the ability is turned on **/
    protected void onActivate(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player)
    {
    }

    public final void deactivate(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player)
    {
        this.onDeactivate(server, sanctuary, player);
        this.onDeactivateCooldown(server, sanctuary, player);
    }

    /** Action on performed when the ability is turned off **/
    protected void onDeactivate(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player)
    {
    }

    protected void onDeactivateCooldown(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player)
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

    public final void use(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player)
    {
        if (onUse(server, sanctuary, player)) {
            onUseCooldown(server, sanctuary, player);
        }
    }

    protected boolean onUse(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player)
    {
        return true;
    }

    protected void onUseCooldown(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player)
    {
    }


    public JsonObject serialize()
    {
        JsonObject json = new JsonObject();

        json.add("name", new JsonPrimitive(this.name));
        json.add("active", new JsonPrimitive(this.active));

        return json;
    }

    public boolean deserializeExtra(JsonObject json)
    {
        return true;
    }

    public static Optional<GroveAbility> deserialize(JsonObject json)
    {
        String name = JSONHelper.getString(json, "name");
        Optional<Boolean> active = JSONHelper.getBoolean(json, "active");

        if (name != null) {
            Optional<GroveAbility> ability = GroveAbilities.getByName(name);

            if (ability.isPresent()) {
                if (!ability.get().deserializeExtra(json))
                    return Optional.empty();

                ability.get().active = active.isPresent() && active.get();

                return ability;
            }
        }

        return Optional.empty();
    }

    public static abstract class AutomaticGroveAbility extends GroveAbility {
        public AutomaticGroveAbility(String name, boolean autodeactivate, boolean defaultAllow, boolean autoInstalled, boolean forbidden) {
            super(name, true, autodeactivate, defaultAllow, autoInstalled, forbidden);
        }
    }

    public static abstract class ManualGroveAbility extends GroveAbility {
        public ManualGroveAbility(String name, boolean defaultAllow, boolean autoInstalled, boolean forbidden) {
            super(name, false, false, defaultAllow, autoInstalled, forbidden);
        }
    }


    public static DataResult<GroveAbility> validate(String name)
    {
        Optional<GroveAbility> ability = GroveAbilities.getByName(name);

        return ability.map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Not a valid Grove Ability: " + name));
    }
}
