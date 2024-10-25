package github.xevira.groves.util;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import github.xevira.groves.Registration;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class WaxHelper {
    public static final Supplier<BiMap<Block, Block>> UNWAXED_TO_WAXED = Suppliers.memoize(
            () -> ImmutableBiMap.<Block, Block>builder()
                    .put(Registration.MOONSTONE_BRICKS_BLOCK, Registration.WAXED_MOONSTONE_BRICKS_BLOCK)
                    .put(Registration.MOONSTONE_BRICK_SLAB_BLOCK, Registration.WAXED_MOONSTONE_BRICK_SLAB_BLOCK)
                    .put(Registration.MOONSTONE_BRICK_STAIRS_BLOCK, Registration.WAXED_MOONSTONE_BRICK_STAIRS_BLOCK)
                    .put(Registration.MOONSTONE_BRICK_WALL_BLOCK, Registration.WAXED_MOONSTONE_BRICK_WALL_BLOCK)
                    .put(Registration.CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK, Registration.WAXED_CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK)
                    .put(Registration.CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK, Registration.WAXED_CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK)
                    .put(Registration.CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK, Registration.WAXED_CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK)
                    .put(Registration.CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK, Registration.WAXED_CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK)
                    .put(Registration.CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK, Registration.WAXED_CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK)
                    .put(Registration.CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK, Registration.WAXED_CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK)
                    .put(Registration.CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK, Registration.WAXED_CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK)
                    .put(Registration.CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK, Registration.WAXED_CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK)
                    .put(Registration.CRACKED_MOONSTONE_BRICKS_BLOCK, Registration.WAXED_CRACKED_MOONSTONE_BRICKS_BLOCK)
                    .build()
    );

    public static final Supplier<BiMap<Block, Block>> WAXED_TO_UNWAXED = Suppliers.memoize(() -> (UNWAXED_TO_WAXED.get()).inverse());

    public static Optional<Block> getWaxedBlock(Block block)
    {
        return Optional.ofNullable((UNWAXED_TO_WAXED.get()).get(block));
    }

    public static Optional<BlockState> getWaxedBlockState(BlockState state)
    {
        return Optional.ofNullable((UNWAXED_TO_WAXED.get()).get(state.getBlock())).map(block -> block.getStateWithProperties(state));
    }

    public static Optional<BlockState> getUnwaxedBlockState(BlockState state)
    {
        return Optional.ofNullable((WAXED_TO_UNWAXED.get()).get(state.getBlock())).map(block -> block.getStateWithProperties(state));
    }

    public static boolean applyWax(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
    {
        if (stack.isOf(Items.HONEYCOMB)) {
            Optional<BlockState> optional = getWaxedBlockState(state);
            if (optional.isPresent()) {
                if (player instanceof ServerPlayerEntity) {
                    Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity)player, pos, stack);
                }

                stack.decrementUnlessCreative(1, player);
                world.setBlockState(pos, optional.get(), Block.NOTIFY_ALL_AND_REDRAW);
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, optional.get()));
                world.syncWorldEvent(player, WorldEvents.BLOCK_WAXED, pos, 0);

                return true;
            }
        }

        return false;
    }

    public static boolean stripWax(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof AxeItem) {
            Optional<BlockState> optional = getUnwaxedBlockState(state);
            if (optional.isPresent()) {
                world.playSound(player, pos, SoundEvents.ITEM_AXE_WAX_OFF, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.syncWorldEvent(player, WorldEvents.WAX_REMOVED, pos, 0);
                if (player instanceof ServerPlayerEntity) {
                    Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity) player, pos, stack);
                }
                world.setBlockState(pos, optional.get(), Block.NOTIFY_ALL_AND_REDRAW);
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, optional.get()));
                if (player != null) {
                    stack.damage(1, player, LivingEntity.getSlotForHand(hand));
                }

                return true;
            }
        }

        return false;
    }



}
