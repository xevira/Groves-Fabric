package github.xevira.groves.sanctuary.ability;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import github.xevira.groves.Groves;
import github.xevira.groves.poi.GrovesPOI;
import github.xevira.groves.sanctuary.GroveAbility;
import github.xevira.groves.util.JSONHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class SummonDruidAbility extends GroveAbility {
    private long waitSeconds = -1L;
    private long lastSummonTime = -1L;

    // Special case Manual
    public SummonDruidAbility() {
        super("summon_druid", false, true, true, true, false);
    }

    @Override
    public Supplier<? extends GroveAbility> getConstructor() {
        return SummonDruidAbility::new;
    }

    @Override
    public @Nullable Item getRecipeIngredient() {
        return null;
    }

    @Override
    public String getEnglishTranslation() {
        return "Summon Druid";
    }

    @Override
    public String getEnglishLoreTranslation() {
        return "Calls forth a wandering druid to provide you with resources.";
    }

    @Override
    public String getEnglishStartCostTranslation() {
        return "";
    }

    @Override
    public String getEnglishTickCostTranslation() {
        return "";
    }

    @Override
    public String getEnglishUseCostTranslation() {
        return "";
    }

    @Override
    public void sendFailure(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player) {

    }

    @Override
    protected void onActivate(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player) {
        this.waitSeconds = rng.nextBetween(15, 30);
        Groves.LOGGER.info("SummonDruid: waitSeconds = {}", this.waitSeconds);
    }

    @Override
    protected void onDeactivate(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player) {
        this.lastSummonTime = sanctuary.getWorld().getTimeOfDay();
        ServerPlayerEntity owner = sanctuary.getOwnerPlayer();
        // Summon the druid
        if (owner != null)
            owner.sendMessage(Text.literal("A wandering druid appears!  ").append(Text.literal("*TADA!*").formatted(Formatting.GREEN, Formatting.ITALIC)), false);
    }

    @Override
    protected void onDeactivateCooldown(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player) {
        int days = rng.nextBetween(1, 4);

        ServerWorld world = sanctuary.getWorld();
        long time = world.getTimeOfDay();

        // Back to the last dawn, then add DAYS days.
        setCooldown(world, days * 24000L - (time % 24000));
    }

    @Override
    public boolean onServerTick(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary) {
        return (--this.waitSeconds <= 0);
    }

    @Override
    public boolean canUse(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player) {
        return !isActive() && !inCooldown(sanctuary.getWorld());
    }

    @Override
    protected boolean onUse(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player) {
        this.activate(server, sanctuary, player);
        return false;
    }

    @Override
    public void serializeExtra(JsonObject json) {
        JsonObject extra = new JsonObject();

        if (this.waitSeconds > 0)
            extra.add("waitSeconds", new JsonPrimitive(this.waitSeconds));

        if (this.lastSummonTime > 0)
            extra.add("lastSummonTime", new JsonPrimitive(this.lastSummonTime));

        json.add("extra", extra);
    }

    @Override
    public boolean deserializeExtra(JsonObject json) {
        JsonObject extra = JSONHelper.getObject(json, "extra").orElse(null);
        if (extra != null)
        {
            this.waitSeconds = JSONHelper.getLong(extra, "waitSeconds").orElse(0L);
            this.lastSummonTime = JSONHelper.getLong(extra, "lastSummonTime").orElse(0L);
        }
        return true;
    }
}
