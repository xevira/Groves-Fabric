package github.xevira.groves.events;

import github.xevira.groves.Groves;
import github.xevira.groves.util.WaxHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ModUseItemEvents {
    public static ActionResult onUseItem(PlayerEntity player, World world, Hand hand)
    {
        ItemStack stack = player.getStackInHand(hand);
        BlockHitResult hit = (BlockHitResult)player.raycast(player.getBlockInteractionRange(), 0, false);
        if (hit.getType() == HitResult.Type.BLOCK)
        {
            BlockPos pos = hit.getBlockPos();
            BlockState state = world.getBlockState(pos);

            if (WaxHelper.applyWax(stack, state, world, pos, player, hand, hit))
                return ActionResult.SUCCESS;

            if (WaxHelper.stripWax(stack, state, world, pos, player, hand, hit))
                return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}
