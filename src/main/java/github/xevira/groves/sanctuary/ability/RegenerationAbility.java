package github.xevira.groves.sanctuary.ability;

import github.xevira.groves.Groves;
import github.xevira.groves.sanctuary.GroveAbility;
import github.xevira.groves.sanctuary.GroveSanctuary;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class RegenerationAbility extends GroveAbility.AutomaticGroveAbility {
    public static final int DURATION = 20;

    public RegenerationAbility() {
        super("regeneration", true, true, false, false, 2);
    }

    @Override
    public Supplier<? extends GroveAbility> getConstructor() {
        return RegenerationAbility::new;
    }

    @Override
    public @Nullable Item getRecipeIngredient(int rank) {
        return switch(rank)
        {
            case 1 -> Items.GHAST_TEAR;
            case 2 -> null;             // TODO: Add Ghast Heart
            default -> null;
        };
    }

    @Override
    public String getEnglishTranslation() {
        return "Regeneration";
    }

    @Override
    public String getEnglishLoreTranslation(int rank) {
        if (rank == 2)
            return "Provides a Regeneration II effect while inside your Grove Sanctuary.";
        return "Provides a Regeneration effect while inside your Grove Sanctuary.  Level is based upon rank.";
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
    public boolean canActivate(MinecraftServer server, GroveSanctuary sanctuary, PlayerEntity player) {
        return sanctuary.getStoredSunlight() >= startCost();
    }

    @Override
    public void sendFailure(MinecraftServer server, GroveSanctuary sanctuary, PlayerEntity player) {
        sendError(player, Groves.text("text", "ability.not_enough_sunlight.activate", startCost()), false);
    }

    @Override
    protected void onActivate(MinecraftServer server, GroveSanctuary sanctuary, PlayerEntity player) {
        // TODO: play a sound on the client
        ServerPlayerEntity owner = sanctuary.getOwnerPlayer();

        applyStatusEffect(sanctuary, owner);
        sanctuary.useSunlight(startCost());
    }

    @Override
    protected void onDeactivate(MinecraftServer server, GroveSanctuary sanctuary, PlayerEntity player) {
        // TODO: play a sound on the client
    }

    @Override
    public boolean onServerTick(MinecraftServer server, GroveSanctuary sanctuary) {
        if (sanctuary.getStoredSunlight() < tickCost())
            return true;

        ServerPlayerEntity owner = sanctuary.getOwnerPlayer();

        applyStatusEffect(sanctuary, owner);
        sanctuary.useSunlight(tickCost());
        return false;
    }

    private void applyStatusEffect(GroveSanctuary sanctuary, ServerPlayerEntity player)
    {
        if (player != null && sanctuary.contains(player.getBlockPos())) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, DURATION * 20, getRank() - 1, true, false));
        }
    }
}
