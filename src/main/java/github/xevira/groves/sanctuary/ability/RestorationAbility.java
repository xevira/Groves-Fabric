package github.xevira.groves.sanctuary.ability;

import github.xevira.groves.Groves;
import github.xevira.groves.poi.GrovesPOI;
import github.xevira.groves.sanctuary.GroveAbility;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Hand;

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
    public long useCost() {
        return 100L;
    }

    @Override
    public void sendFailure(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player) {
        ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);

        // Empty hand
        if (stack.isEmpty())
            player.sendMessage(Groves.text("text", "ability.empty_hand"), false);
        else if (stack.getMaxDamage() < 1)
            player.sendMessage(Groves.text("text", "ability.no_durability"), false);
        else if (stack.getDamage() < 1)
            player.sendMessage(Groves.text("text", "ability.no_damage"), false);
        else {
            long cost = useCost() * stack.getDamage();
            player.sendMessage(Groves.text("text", "ability.not_enough_sunlight.use", cost), false);
        }
    }

    @Override
    public boolean onServerTick(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary) {
        return false;
    }

    @Override
    public boolean canUse(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player) {
        ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);

        // Empty hand
        if (stack.isEmpty())
            return false;

        // Has no durability
        if (stack.getMaxDamage() < 1)
            return false;

        // Doesn't need repair
        if (stack.getDamage() < 1)
            return false;

        long cost = useCost() * stack.getDamage();

        return sanctuary.getStoredSunlight() >= cost;
    }

    @Override
    public boolean onUse(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player) {
        ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);

        if (!stack.isEmpty() && stack.getDamage() > 0)
        {
            long cost = useCost() * stack.getDamage();

            stack.setDamage(0);
            sanctuary.useSunlight(cost);

            // TODO: play sound
            player.sendMessage(Groves.text("text", "ability.item_restored"), false);
        }
        return true;
    }
}
