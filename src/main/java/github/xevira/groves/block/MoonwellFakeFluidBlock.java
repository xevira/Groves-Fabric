package github.xevira.groves.block;

import com.mojang.serialization.MapCodec;
import github.xevira.groves.Registration;
import github.xevira.groves.block.entity.MoonwellMultiblockMasterBlockEntity;
import github.xevira.groves.block.entity.MoonwellMultiblockSlaveBlockEntity;
import github.xevira.groves.block.multiblock.Moonwell;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MoonwellFakeFluidBlock extends TransparentBlock implements BlockEntityProvider {
    public static final MapCodec<MoonwellFakeFluidBlock> CODEC = createCodec(MoonwellFakeFluidBlock::new);

    public MoonwellFakeFluidBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends TransparentBlock> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return Registration.MOONWELL_FAKE_FLUID_BLOCK_ENTITY.instantiate(pos, state);
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        Moonwell.breakSlaveBlock(state, world, pos, newState, moved, super::onStateReplaced);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        Moonwell.onUse(state, world, pos, player, hit);

        return ActionResult.SUCCESS;
    }

    @Override
    protected boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        return true;
    }
}
