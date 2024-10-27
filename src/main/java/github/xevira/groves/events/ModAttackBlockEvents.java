package github.xevira.groves.events;

import github.xevira.groves.util.OwnedBlock;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ModAttackBlockEvents implements AttackBlockCallback {
    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
        if (player.isSpectator()) return ActionResult.PASS;

        if (player.isCreativeLevelTwoOp()) return ActionResult.PASS;

        BlockState state = world.getBlockState(pos);

        if (state.getBlock() instanceof OwnedBlock owned)
        {
            if (!owned.canBreakBlock(player, world, pos))
                return ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }
}
