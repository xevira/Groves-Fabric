package github.xevira.groves.item;

import github.xevira.groves.Groves;
import github.xevira.groves.Registration;
import github.xevira.groves.ServerConfig;
import github.xevira.groves.block.multiblock.Moonwell;
import github.xevira.groves.poi.GrovesPOI;
import github.xevira.groves.sanctuary.GroveSanctuary;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class MoonPhialItem extends Item {
    private static final Map<Integer, String> PHASE_NAMES = new HashMap<>();

    private static int moonphaseIndex = 0;
    private static String moonphase = "Null";

    static {
        PHASE_NAMES.put(0, "full_moon");
        PHASE_NAMES.put(1, "full_moon_day");
        PHASE_NAMES.put(2, "waning_gibbous");
        PHASE_NAMES.put(3, "waning_gibbous_day");
        PHASE_NAMES.put(4, "third_quarter");
        PHASE_NAMES.put(5, "third_quarter_day");
        PHASE_NAMES.put(6, "waning_crescent");
        PHASE_NAMES.put(7, "waning_crescent_day");
        PHASE_NAMES.put(8, "new_moon");
        PHASE_NAMES.put(9, "new_moon_day");
        PHASE_NAMES.put(10, "waxing_crescent");
        PHASE_NAMES.put(11, "waxing_crescent_day");
        PHASE_NAMES.put(12, "first_quarter");
        PHASE_NAMES.put(13, "first_quarter_day");
        PHASE_NAMES.put(14, "waxing_gibbous");
        PHASE_NAMES.put(15, "waxing_gibbous_day");
    }

    public static final Text WORLD_NO_TIME_TEXT = Groves.text("text", "moon_phial.world_no_time");
    public static final Text WORLD_DAYTIME_TEXT = Groves.text("text", "moon_phial.world_daytime");
    public static final Text WRONG_PHASE_TEXT = Groves.text("text", "moon_phial.wrong_phase");
    public static final Text NOT_OWNER_TEXT = Groves.text("text", "moon_phial.grove.not_owner");
    public static final Text NOT_IN_GROVE_TEXT = Groves.text("text", "moon_phial.not_in_grove");
    public static final Text MOONWELL_EXISTS_TEXT = Groves.text("text", "moon_phial.moonwell_exists");

    public MoonPhialItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null) return ActionResult.FAIL;

        World world = context.getWorld();

        if (!world.isClient)
        {
            BlockPos pos = context.getBlockPos();
            BlockState state = world.getBlockState(pos);

            if (state.isOf(Blocks.CAULDRON))
            {
                // TODO: Check if location meets the requirement
                if (world.getDimension().hasFixedTime())
                {
                    player.sendMessage(WORLD_NO_TIME_TEXT, false);
                    return ActionResult.FAIL;
                }
                else if (world.isDay())
                {
                    player.sendMessage(WORLD_DAYTIME_TEXT, false);
                    return ActionResult.FAIL;
                }
                else if (moonphaseIndex >= 4 && moonphaseIndex <= 12)
                {
                    player.sendMessage(WRONG_PHASE_TEXT, false);
                    return ActionResult.FAIL;
                }
                else {
                    Optional<GroveSanctuary> sanc = GrovesPOI.getSanctuary((ServerWorld)world, pos);

                    if (sanc.isPresent())
                    {
                        GroveSanctuary sanctuary = sanc.get();

                        if (!sanctuary.isOwner(player)) {
                            player.sendMessage(NOT_OWNER_TEXT, false);
                            return ActionResult.FAIL;

                        } else if (sanctuary.hasMoonwell()) {
                            player.sendMessage(MOONWELL_EXISTS_TEXT, false);
                            return ActionResult.FAIL;

                        } else if (sanctuary.getStoredSunlight() < ServerConfig.getFormMoonwellCost()) {
                            player.sendMessage(MOONWELL_EXISTS_TEXT, false);
                            return ActionResult.FAIL;

                        } else if (Moonwell.tryForm(player, world, pos)) {
                            sanctuary.setMoonwell(pos);
                            sanctuary.useSunlight(ServerConfig.getFormMoonwellCost());
                            context.getStack().decrementUnlessCreative(1, player);
                            world.playSound(null, pos, Registration.MOONWELL_ACTIVATE_SOUND, SoundCategory.BLOCKS, 1.0f, 1.0f);
                        }

                        return ActionResult.SUCCESS;
                    }
                    else
                    {
                        player.sendMessage(NOT_IN_GROVE_TEXT, false);
                        return ActionResult.FAIL;
                    }
                }
            }
        }

        return super.useOnBlock(context);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        BlockHitResult blockHitResult = raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);
        if (blockHitResult.getType() == HitResult.Type.MISS) {
            return ActionResult.PASS;
        } else if (blockHitResult.getType() != HitResult.Type.BLOCK) {
            return ActionResult.PASS;
        } else {
            BlockPos blockPos = blockHitResult.getBlockPos();
            if (!world.canPlayerModifyAt(user, blockPos)) {
                return ActionResult.FAIL;
            } else {
                BlockState blockState = world.getBlockState(blockPos);
                if (blockState.isOf(Blocks.WATER)) {
                    BlockState blessed = Registration.BLESSED_MOON_WATER_BLOCK.getDefaultState();
                    world.setBlockState(blockPos, blessed, Block.NOTIFY_ALL_AND_REDRAW);
                    itemStack.decrementUnlessCreative(1, user);

                    return ActionResult.SUCCESS;
                }

                return ActionResult.PASS;
            }
        }
    }


    public static void updateLunarPhase(World world)
    {
        if (!world.isClient)
        {
            moonphaseIndex = getLunarPhase(world);
            moonphase = PHASE_NAMES.get(moonphaseIndex);
        }
    }

    public static int getLunarPhase(World world)
    {
        if (world == null) return 0;

        return ((world.getMoonPhase()) * 2 + (world.isDay() ? 1 : 0));
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return moonphaseIndex < 4 || moonphaseIndex > 12;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Groves.text("lore", "lunar_phase." + moonphase).formatted(Formatting.BLUE));
        }

        super.appendTooltip(stack, context, tooltip, type);
    }

    @Environment(EnvType.CLIENT)
    public static float getModelPredicate(ItemStack stack, ClientWorld world, LivingEntity entity, int seed) {
        if (stack == null) return 0.0f;

        return moonphaseIndex * 0.0625f;
    }

}
