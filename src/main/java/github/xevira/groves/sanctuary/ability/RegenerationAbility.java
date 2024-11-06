package github.xevira.groves.sanctuary.ability;

import github.xevira.groves.Groves;
import github.xevira.groves.poi.GrovesPOI;
import github.xevira.groves.sanctuary.GroveAbility;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class RegenerationAbility extends GroveAbility.AutomaticGroveAbility {
    public static final int DURATION = 20;

    public RegenerationAbility() {
        super("regeneration", true, true, false, false);
    }

    @Override
    public Supplier<? extends GroveAbility> getConstructor() {
        return RegenerationAbility::new;
    }

    @Override
    public @Nullable Item getRecipeIngredient() {
        return Items.GHAST_TEAR;
    }

    @Override
    public String getEnglishTranslation() {
        return "Regeneration";
    }

    @Override
    public String getEnglishLoreTranslation() {
        return "Provides a Regeneration effect while inside your Grove Sanctuary.";
    }

    @Override
    public String getEnglishStartCostTranslation() {
        return "%s sunlight to activate.";
    }

    @Override
    public String getEnglishTickCostTranslation() {
        return "%s sunlight per second per enabled chunk to maintain.";
    }

    @Override
    public String getEnglishUseCostTranslation() {
        return null;
    }

    @Override
    public long startCost() {
        return 5000L;
    }

    @Override
    public long tickCost() {
        return 200L;
    }

    @Override
    public boolean canActivate(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player) {
        return sanctuary.getStoredSunlight() >= startCost();
    }

    @Override
    public void sendFailure(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player) {
        player.sendMessage(Groves.text("text", "ability.not_enough_sunlight.activate", startCost()), false);
    }

    @Override
    protected void onActivate(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player) {
        // TODO: play a sound on the client
        ServerPlayerEntity owner = sanctuary.getOwnerPlayer();

        applyStatusEffect(sanctuary, owner);
        sanctuary.useSunlight(startCost());
    }

    @Override
    protected void onDeactivate(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player) {
        // TODO: play a sound on the client
    }

    @Override
    public boolean onServerTick(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary) {
        if (sanctuary.getStoredSunlight() < tickCost())
            return true;

        ServerPlayerEntity owner = sanctuary.getOwnerPlayer();

        applyStatusEffect(sanctuary, owner);
        sanctuary.useSunlight(tickCost());
        return false;
    }

    private static void applyStatusEffect(GrovesPOI.GroveSanctuary sanctuary, ServerPlayerEntity player)
    {
        if (player != null && sanctuary.contains(player.getBlockPos())) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, DURATION * 20, 0, true, false));
        }
    }
}
