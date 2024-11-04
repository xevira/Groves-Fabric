package github.xevira.groves.item;

import com.mojang.datafixers.util.Either;
import github.xevira.groves.Groves;
import github.xevira.groves.network.ImprintPayload;
import github.xevira.groves.poi.GrovesPOI;
import github.xevira.groves.sanctuary.GroveAbilities;
import github.xevira.groves.sanctuary.GroveAbility;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UnlockScrollItem extends Item {
    private GroveAbility ability;

    public UnlockScrollItem(GroveAbility ability, Settings settings) {
        super(settings);

        this.ability = ability;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return this.ability != null;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public boolean onStoppedUsing(ItemStack stack, @NotNull World world, LivingEntity user, int remainingUseTicks) {
        if (!world.isClient && this.ability != null) {
            if (user instanceof ServerPlayerEntity player) {
                int i = this.getMaxUseTime(stack, user) - remainingUseTicks;
                float f = getPullProgress(i);
                if (f > 0.9f) {
                    GrovesPOI.GroveSanctuary sanctuary = GrovesPOI.getSanctuary(player).orElse(null);

                    if (sanctuary != null)
                    {
                        if (sanctuary.hasAbility(this.ability.getName()))
                        {
                            MutableText msg = Text.literal("Your sanctuary already has ");
                            msg.append(Groves.text("name", "ability." + this.ability.getName()).formatted(Formatting.RED));
                            msg.append(".");

                            player.sendMessage(msg, false);
                        }
                        else {
                            sanctuary.installAbility(this.ability);

                            MutableText msg = Groves.text("name", "ability." + this.ability.getName()).formatted(Formatting.GREEN);
                            msg.append(" installed.");

                            player.sendMessage(msg, false);
                            // TODO: Play sound

                            stack.decrementUnlessCreative(1, player);
                        }

                        return true;
                    }
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
    public UseAction getUseAction(ItemStack stack) {
        if (this.ability != null)
            return UseAction.BOW;
        else
            return UseAction.NONE;
    }


    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return ActionResult.CONSUME;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (this.ability != null)
        {
            tooltip.add(Text.translatable(this.translationKey + ".lore"));
            if (ability.startCost() > 0 || ability.tickCost() > 0 || ability.useCost() > 0) {
                tooltip.add(Text.empty());

                if (ability.startCost() > 0)
                    tooltip.add(Groves.text("tooltip", "ability.cost.start").formatted(Formatting.YELLOW)
                            .append(" ")
                            .append(Text.translatable(this.translationKey + ".cost.start", ability.startCost())));

                if (ability.tickCost() > 0)
                    tooltip.add(Groves.text("tooltip", "ability.cost.tick").formatted(Formatting.YELLOW)
                            .append(" ")
                            .append(Text.translatable(this.translationKey + ".cost.tick", ability.tickCost())));

                if (ability.useCost() > 0)
                    tooltip.add(Groves.text("tooltip", "ability.cost.use").formatted(Formatting.YELLOW)
                            .append(" ")
                            .append(Text.translatable(this.translationKey + ".cost.use", ability.useCost())));
            }
        }

        super.appendTooltip(stack, context, tooltip, type);
    }
}
