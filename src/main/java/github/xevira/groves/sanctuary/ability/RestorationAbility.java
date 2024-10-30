package github.xevira.groves.sanctuary.ability;

import github.xevira.groves.Groves;
import github.xevira.groves.poi.GrovesPOI;
import github.xevira.groves.sanctuary.GroveAbility;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;

import java.util.function.Supplier;

public class RestorationAbility extends GroveAbility.ManualGroveAbility {
    public RestorationAbility() {
        super("restoration", true);
    }

    @Override
    public Supplier<? extends GroveAbility> getConstructor() {
        return RestorationAbility::new;
    }

    @Override
    public long startCost() {
        return -1;
    }

    @Override
    public long tickCost() {
        return -1;
    }

    @Override
    public long useCost() {
        return 10000L;
    }

    @Override
    public boolean canActivate(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player) {
        return false;
    }

    @Override
    public void sendFailure(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player) {

    }

    @Override
    public void onActivate(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player) {
        player.sendMessage(Groves.text("text", "ability.not_enough_sunlight", useCost()), false);

    }

    @Override
    public void onDeactivate(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player) {

    }

    @Override
    public boolean onServerTick(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary) {
        return false;
    }

    @Override
    public boolean canUse(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player) {
        return sanctuary.getStoredSunlight() >= useCost();
    }

    @Override
    public boolean onUse(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player) {
        return true;
    }
}
