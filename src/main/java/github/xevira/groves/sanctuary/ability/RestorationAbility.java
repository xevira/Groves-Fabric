package github.xevira.groves.sanctuary.ability;

import github.xevira.groves.Groves;
import github.xevira.groves.poi.GrovesPOI;
import github.xevira.groves.sanctuary.GroveAbility;
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
        super("restoration", true, false, false);
    }

    @Override
    public Supplier<? extends GroveAbility> getConstructor() {
        return RestorationAbility::new;
    }

    @Override
    public @Nullable Item getRecipeIngredient() {
        return Items.ANVIL;
    }

    @Override
    public String getEnglishTranslation() {
        return "Restoration";
    }

    @Override
    public String getEnglishLoreTranslation() {
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
        return 100L;
    }

    @Override
    public void sendFailure(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player) {
        ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);

        // Empty hand
        if (stack.isEmpty())
            player.sendMessage(Groves.text("text", "ability.empty_hand"), false);
        else if (!stack.isDamageable())
            player.sendMessage(Groves.text("text", "ability.no_durability"), false);
        else if (!stack.isDamaged())
            player.sendMessage(Groves.text("text", "ability.no_damage"), false);
        else {
            player.sendMessage(Groves.text("text", "ability.not_enough_sunlight.use", useCost()), false);
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
        if (!stack.isDamageable())
            return false;

        // Doesn't need repair
        if (!stack.isDamaged())
            return false;

        return sanctuary.getStoredSunlight() >= useCost();
    }

    @Override
    protected boolean onUse(MinecraftServer server, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player) {
        ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);

        if (!stack.isEmpty() && stack.getDamage() > 0)
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
