package github.xevira.groves.item;

import github.xevira.groves.Groves;
import github.xevira.groves.poi.GrovesPOI;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class ImprintingSigilItem extends Item {
    private static final Text ALREADY_EXISTS_TEXT = Groves.text("text", "imprinting.already_exists");
    private static final Text HAS_GROVE_TEXT = Groves.text("text", "imprinting.has_grove");

    private final boolean enchanted;

    public ImprintingSigilItem(boolean enchanted, Settings settings) {
        super(settings);

        this.enchanted = enchanted;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return this.enchanted;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override

    public boolean onStoppedUsing(ItemStack stack, @NotNull World world, LivingEntity user, int remainingUseTicks) {
        if (!world.isClient) {
            if (user instanceof PlayerEntity player) {
                int i = this.getMaxUseTime(stack, user) - remainingUseTicks;
                float f = getPullProgress(i);
                if (f > 0.9f) {
                    if (GrovesPOI.isValidGroveLocation((ServerWorld)world, player.getBlockPos(), this.enchanted)) {
                        GrovesPOI.ImprintSanctuaryResult result = GrovesPOI.imprintSanctuary(player, (ServerWorld)world, player.getBlockPos(), this.enchanted);

                        switch(result)
                        {
                            case SUCCESS -> {
                                // TODO: Play Sound
                                // TODO: Give them an item?  A guide book, perhaps?
                                stack.decrementUnlessCreative(1, player);
                            }

                            case ALREADY_EXISTS -> {

                            }

                            case ALREADY_HAS_GROVE -> {

                            }
                        }
                    }

                    player.incrementStat(Stats.USED.getOrCreateStat(this));
                    return true;
                }
            }
        }

        return false;
    }

    public static float getPullProgress(int useTicks) {
        float f = (float)useTicks / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }
    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        user.setCurrentHand(hand);
        return ActionResult.CONSUME;
    }
}
