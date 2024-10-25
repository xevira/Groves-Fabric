package github.xevira.groves.block;

import com.mojang.serialization.MapCodec;
import github.xevira.groves.Registration;
import github.xevira.groves.block.entity.MoonwellMultiblockMasterBlockEntity;
import github.xevira.groves.block.entity.MoonwellMultiblockSlaveBlockEntity;
import github.xevira.groves.util.ServerTickableBlockEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MoonwellBasinBlock extends BlockWithEntity {
    public static final MapCodec<MoonwellBasinBlock> CODEC = createCodec(MoonwellBasinBlock::new);
    private static final VoxelShape RAYCAST_SHAPE = createCuboidShape(2.0, 4.0, 2.0, 14.0, 16.0, 14.0);
    protected static final VoxelShape OUTLINE_SHAPE = VoxelShapes.combineAndSimplify(
            VoxelShapes.fullCube(),
            VoxelShapes.union(
                    createCuboidShape(0.0, 0.0, 4.0, 16.0, 3.0, 12.0),
                    createCuboidShape(4.0, 0.0, 0.0, 12.0, 3.0, 16.0),
                    createCuboidShape(2.0, 0.0, 2.0, 14.0, 3.0, 14.0),
                    RAYCAST_SHAPE
            ),
            BooleanBiFunction.ONLY_FIRST
    );

    public MoonwellBasinBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    protected VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return RAYCAST_SHAPE;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return Registration.MOONWELL_MULTIBLOCK_MASTER_BLOCK_ENTITY.instantiate(pos, state);
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!moved && !state.isOf(newState.getBlock()))
        {
            if (world.getBlockEntity(pos) instanceof MoonwellMultiblockMasterBlockEntity masterBlockEntity)
            {
                masterBlockEntity.breakMultiblock(true);
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            if (world.getBlockEntity(pos) instanceof MoonwellMultiblockMasterBlockEntity master)
            {
                long amount = master.getMoonlightAmount();
                int percent = master.getMoonlightPercent();

                player.sendMessage(Text.literal(String.format("Total Stored Moonlight: %d (%d%%)", amount, percent)));
            }
        }

        return ActionResult.success(world.isClient);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return ServerTickableBlockEntity.getTicker(world);
    }
}
