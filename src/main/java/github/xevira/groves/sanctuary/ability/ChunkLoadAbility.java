package github.xevira.groves.sanctuary.ability;

import github.xevira.groves.Groves;
import github.xevira.groves.poi.GrovesPOI;
import github.xevira.groves.sanctuary.GroveAbility;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class ChunkLoadAbility extends GroveAbility.AutomaticGroveAbility {
    public static final long START_COST = 100000L;
    public static final long TICK_COST = 100L;

    public ChunkLoadAbility() {
        super("chunk_load", true, true);
    }

    @Override
    public Supplier<? extends GroveAbility> getConstructor() {
        return ChunkLoadAbility::new;
    }

    @Override
    public @Nullable Item getRecipeIngredient() {
        return Items.ENDER_PEARL;
    }

    @Override
    public String getEnglishTranslation() {
        return "Chunk Load";
    }

    @Override
    public String getEnglishLoreTranslation() {
        return  "Allows your Grove Sanctuary to load enabled chunks.";
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
        return 100000L;
    }

    @Override
    public long tickCost() {
        return 100L;
    }

    @Override
    public boolean canActivate(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player) {
        return sanctuary.getStoredSunlight() >= startCost();
    }

    @Override
    public void sendFailure(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player)
    {
        player.sendMessage(Groves.text("text", "ability.not_enough_sunlight.activate", startCost()), false);
    }

    @Override
    public void onActivate(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player) {
        // TODO:
        sanctuary.setChunkLoading(true);
        sanctuary.useSunlight(startCost());
        player.sendMessage(Text.literal("Designated chunks are now force loaded."), false);
    }

    @Override
    public void onDeactivate(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player) {
        sanctuary.setChunkLoading(false);
        player.sendMessage(Text.literal("Chunk loading in sanctuary deactivated."), false);
    }

    @Override
    public boolean onServerTick(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary) {
        long cost = tickCost() * sanctuary.totalChunks();

        if (sanctuary.getStoredSunlight() < cost)
            return true;

        sanctuary.useSunlight(cost);
        return false;
    }
}
