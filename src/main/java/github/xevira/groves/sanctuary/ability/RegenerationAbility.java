package github.xevira.groves.sanctuary.ability;

import github.xevira.groves.Groves;
import github.xevira.groves.poi.GrovesPOI;
import github.xevira.groves.sanctuary.GroveAbility;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public class RegenerationAbility extends GroveAbility.AutomaticGroveAbility {
    public static final int DURATION = 20;

    public RegenerationAbility() {
        super("regeneration", true, true);
    }

    @Override
    public Supplier<? extends GroveAbility> getConstructor() {
        return RegenerationAbility::new;
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
    public void onActivate(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player) {
        // TODO: play a sound on the client
        ServerPlayerEntity owner = sanctuary.getOwnerPlayer();

        applyStatusEffect(sanctuary, owner);
        sanctuary.useSunlight(startCost());
    }

    @Override
    public void onDeactivate(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player) {
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
