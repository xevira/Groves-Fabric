package github.xevira.groves.item;

import com.mojang.datafixers.util.Either;
import github.xevira.groves.Groves;
import github.xevira.groves.client.event.KeyInputHandler;
import github.xevira.groves.network.ImprintPayload;
import github.xevira.groves.poi.GrovesPOI;
import github.xevira.groves.sanctuary.GroveSanctuary;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
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

    public static final Text EXISTS_ERROR = Groves.text("error", "imprint.exists");
    public static final Text HAS_ERROR = Groves.text("error", "imprint.has");
    public static final Text OWN_ERROR = Groves.text("error", "imprint.own");
    public static final Text ABANDONED_ERROR = Groves.text("error", "imprint.abandoned");

    @Override
    public boolean onStoppedUsing(ItemStack stack, @NotNull World world, LivingEntity user, int remainingUseTicks) {
        if (!world.isClient) {
            if (user instanceof ServerPlayerEntity player) {
                int i = this.getMaxUseTime(stack, user) - remainingUseTicks;
                float f = getPullProgress(i);
                if (f > 0.9f) {
                    if (GrovesPOI.isValidGroveLocation((ServerWorld)world, player.getBlockPos(), this.enchanted)) {
                        Either<GroveSanctuary, GrovesPOI.ImprintSanctuaryResult> result = GrovesPOI.imprintSanctuary(player, (ServerWorld)world, player.getBlockPos(), this.enchanted);

                        if (result.left().isPresent()) {

                            GroveSanctuary sanctuary = result.left().get();
                            // TODO: Play Sound
                            // TODO: Give them an item?  A guide book, perhaps?
                            stack.decrementUnlessCreative(1, player);
                            ServerPlayNetworking.send(player, new ImprintPayload());
                            sanctuary.openUI(player);
                        }
                        else if (result.right().isPresent()) {
                            switch(result.right().get())
                            {
                                case ALREADY_EXISTS -> {
                                    player.sendMessage(EXISTS_ERROR, false);
                                }

                                case ALREADY_HAS_GROVE -> {
                                    player.sendMessage(HAS_ERROR, false);
                                }

                                case ALREADY_OWN_GROVE -> {
                                    player.sendMessage(OWN_ERROR, false);
                                }

                                case ABANDONED -> {
                                    player.sendMessage(ABANDONED_ERROR, false);
                                }
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

    public static final MutableText IMPRINT_PREFIX = Groves.text("text", "imprint.successful.prefix");
    public static final MutableText IMPRINT_SUFFIX = Groves.text("text", "imprint.successful.suffix");

    /** Informs the player they have successfully imprinted.  Called this way to access client-only data, namely the player's keybind. **/
    @Environment(EnvType.CLIENT)
    public static void clientImprintSuccessful(ClientPlayerEntity player)
    {
        MutableText msg = IMPRINT_PREFIX;
        msg.append(Text.literal("[" + KeyInputHandler.OPEN_GROVES_UI_KEY.getKeybind().getKeysDisplayString() + "]").formatted(Formatting.GREEN));
        msg.append(IMPRINT_SUFFIX);

        player.sendMessage(msg, false);
    }
}
