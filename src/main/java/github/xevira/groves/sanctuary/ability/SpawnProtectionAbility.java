package github.xevira.groves.sanctuary.ability;

import github.xevira.groves.Groves;
import github.xevira.groves.sanctuary.GroveAbility;
import github.xevira.groves.sanctuary.GroveSanctuary;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class SpawnProtectionAbility extends GroveAbility.AutomaticGroveAbility {
    public SpawnProtectionAbility() {
        super("spawn_protection", true, true, false, false, 5);
    }

    @Override
    public Supplier<? extends GroveAbility> getConstructor() {
        return SpawnProtectionAbility::new;
    }

    @Override
    public @Nullable Item getRecipeIngredient(int rank) {
        return switch(rank)
        {
            case 1 -> Items.TORCH;
            default -> null;
        };
    }

    @Override
    public String getEnglishTranslation() {
        return "Hostile Spawn Protection";
    }

    @Override
    public String getEnglishLoreTranslation(int rank) {
        return "Prevents hostile mobs from spawning within a certain radius of sanctuary chunks.";
    }

    @Override
    public String getEnglishStartCostTranslation() {
        return "%s sunlight to activate.";
    }

    @Override
    public String getEnglishTickCostTranslation() {
        return "%s sunlight per chunk to maintain.";
    }

    @Override
    public String getEnglishUseCostTranslation() {
        return "%s sunlight per spawn blocked.";
    }


    @Override
    public long startCost(int rank) {
        return switch(rank)
        {
            case 2 -> 5000L;
            case 3 -> 2500L;
            case 4 -> 1250L;
            case 5 -> 625L;
            default -> 10000L;
        };
    }

    @Override
    public long tickCost(int rank) {
        return 0;
    }

    @Override
    public long useCost(int rank) {
        return switch(rank)
        {
            case 2 -> 500L;
            case 3 -> 250L;
            case 4 -> 125L;
            case 5 -> 60L;
            default -> 1000L;
        };
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
        sanctuary.useSunlight(startCost());
        player.sendMessage(Text.literal("Hostile spawn protection activated."), false);
    }

    @Override
    protected void onDeactivate(MinecraftServer server, GroveSanctuary sanctuary, PlayerEntity player) {
        sanctuary.setChunkLoading(false);

        if (player == null)
            player = sanctuary.getOwnerPlayer();

        if (player != null)
            player.sendMessage(Text.literal("Hostile spawn protection deactivated."), false);
    }

    @Override
    public boolean onServerTick(MinecraftServer server, GroveSanctuary sanctuary) {
        // Deactivate once it can't block anymore spawns
        return sanctuary.getTotalSunlight() < (useCost() * sanctuary.totalChunks());
    }

    @Override
    public boolean canUse(MinecraftServer server, GroveSanctuary sanctuary, PlayerEntity player) {
        return sanctuary.getTotalSunlight() >= (useCost() * sanctuary.totalChunks());
    }

    @Override
    protected boolean onUse(MinecraftServer server, GroveSanctuary sanctuary, PlayerEntity player) {
        long cost = useCost() * sanctuary.totalChunks();

        sanctuary.useSunlight(cost);
        return true;
    }
}
