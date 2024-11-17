package github.xevira.groves.sanctuary.ability;

import github.xevira.groves.Groves;
import github.xevira.groves.sanctuary.GroveAbility;
import github.xevira.groves.sanctuary.GroveSanctuary;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class RestorationAbility extends GroveAbility.ManualGroveAbility {
    public RestorationAbility() {
        super("restoration", true, false, false, 5);
    }

    @Override
    public Supplier<? extends GroveAbility> getConstructor() {
        return RestorationAbility::new;
    }

    @Override
    public @Nullable Item getRecipeIngredient(int rank)
    {
        return switch(rank)
        {
            case 1 -> Items.ANVIL;
            case 2 -> Items.CHIPPED_ANVIL;  // Am I an ass?
            case 3 -> Items.DAMAGED_ANVIL;  //  .. Yes, yes I am.
            case 4 -> null;
            case 5 -> null;
            default -> null;
        };
    }

    @Override
    public String getEnglishTranslation() {
        return "Restoration";
    }

    @Override
    public String getEnglishLoreTranslation(int rank) {
        return "Uses sunlight to repair your selected item.";
    }

    @Override
    public String getEnglishStartCostTranslation() {
        return null;
    }

    @Override
    public String getEnglishTickCostTranslation() {
        return null;
    }

    @Override
    public String getEnglishUseCostTranslation() {
        return "%s sunlight per point of damage repaired.";
    }


    @Override
    public long useCost() {
        return switch(getRank())
        {
            case 2 -> 75L;
            case 3 -> 50L;
            case 4 -> 25L;
            case 5 -> 10L;
            default -> 100L;
        };
    }

    @Override
    public void sendFailure(MinecraftServer server, GroveSanctuary sanctuary, PlayerEntity player) {
        ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);

        // Empty hand
        if (stack.isEmpty())
            sendError(player, Groves.text("text", "ability.empty_hand"), false);
        else if (!stack.isDamageable())
            sendError(player, Groves.text("text", "ability.no_durability"), false);
        else if (!stack.isDamaged())
            sendError(player, Groves.text("text", "ability.no_damage"), false);
        else {
            sendError(player, Groves.text("text", "ability.not_enough_sunlight.use", useCost()), false);
        }
    }

    @Override
    public boolean onServerTick(MinecraftServer server, GroveSanctuary sanctuary) {
        return false;
    }

    @Override
    public boolean canUse(MinecraftServer server, GroveSanctuary sanctuary, PlayerEntity player) {
        ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);

        // Empty hand
        if (stack.isEmpty())
            return false;

        // Has no durability
        if (!stack.isDamageable())
            return false;

        // Doesn't need repair
        if (!stack.isDamaged())
            return false;

        return sanctuary.getStoredSunlight() >= useCost();
    }

    @Override
    protected boolean onUse(MinecraftServer server, GroveSanctuary sanctuary, PlayerEntity player) {
        ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);

        if (!stack.isEmpty() && stack.isDamaged())
        {
            int damage = stack.getDamage();
            int repair = damage;
            long cost = useCost() * repair;
            long sunlight = sanctuary.getStoredSunlight();

            // Handle partial repairs
            if (cost > sunlight && useCost() > 0) {
                repair = (int) (sunlight / useCost());
                cost = useCost() * repair;
            }

            stack.setDamage(damage - repair);
            sanctuary.useSunlight(cost);

            // TODO: play sound
            if (repair < damage)
                player.sendMessage(Groves.text("text", "ability.item_partially_restored"), false);
            else
                player.sendMessage(Groves.text("text", "ability.item_restored"), false);
        }
        return true;
    }
}
