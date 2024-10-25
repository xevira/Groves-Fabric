package github.xevira.groves.block;

import github.xevira.groves.Registration;
import github.xevira.groves.block.entity.MoonwellMultiblockMasterBlockEntity;
import github.xevira.groves.block.entity.MoonwellMultiblockSlaveBlockEntity;
import github.xevira.groves.block.multiblock.Moonwell;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MoonwellBrickWallBlock extends WallBlock implements BlockEntityProvider {
    public MoonwellBrickWallBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return Registration.MOONWELL_MULTIBLOCK_SLAVE_BLOCK_ENTITY.instantiate(pos, state);
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        Moonwell.breakSlaveBlock(state, world, pos, newState, moved, super::onStateReplaced);
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) { return true; }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        Moonwell.randomRevert(state, world, pos, random);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            if (world.getBlockEntity(pos) instanceof MoonwellMultiblockSlaveBlockEntity slave) {
                BlockPos masterPos = slave.getMaster();
                if (masterPos != null) {
                    if (world.getBlockEntity(masterPos) instanceof MoonwellMultiblockMasterBlockEntity master)
                    {
                        long amount = master.getMoonlightAmount();
                        int percent = master.getMoonlightPercent();

                        player.sendMessage(Text.literal(String.format("Total Stored Moonlight: %d (%d%%)", amount, percent)));
                    }
                }
            }
        }

        return ActionResult.success(world.isClient);
    }
}
