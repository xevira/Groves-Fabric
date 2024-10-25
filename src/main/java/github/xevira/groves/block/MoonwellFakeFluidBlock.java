package github.xevira.groves.block;

import com.mojang.serialization.MapCodec;
import github.xevira.groves.Registration;
import github.xevira.groves.block.entity.MoonwellMultiblockMasterBlockEntity;
import github.xevira.groves.block.entity.MoonwellMultiblockSlaveBlockEntity;
import github.xevira.groves.block.multiblock.Moonwell;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
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

public class MoonwellFakeFluidBlock extends BlockWithEntity {
    public static final MapCodec<MoonwellFakeFluidBlock> CODEC = createCodec(MoonwellFakeFluidBlock::new);

    public MoonwellFakeFluidBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
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

    @Override
    protected VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    protected float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    @Override
    protected boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        return true;
    }
}
