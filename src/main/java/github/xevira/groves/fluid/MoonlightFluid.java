package github.xevira.groves.fluid;

import github.xevira.groves.Registration;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

import java.util.Optional;

public abstract class MoonlightFluid extends FlowableFluid {
    @Override
    public Fluid getFlowing() { return Registration.FLOWING_MOONLIGHT_FLUID; }

    @Override
    public Fluid getStill() { return Registration.MOONLIGHT_FLUID; }

    @Override
    public Item getBucketItem() { return Registration.BLESSED_MOON_WATER_BUCKET_ITEM; }

    // TODO: Make this a config option


    @Override
    protected boolean isInfinite(ServerWorld world) {
        return false;
    }

    @Override
    protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
        Block.dropStacks(state, world, pos, blockEntity);
    }

    @Override
    protected int getMaxFlowDistance(WorldView world) { return 4; }

    @Override
    protected int getLevelDecreasePerBlock(WorldView world) { return 1; }

    @SuppressWarnings("deprecation")
    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
        return direction == Direction.DOWN && !fluid.isIn(FluidTags.WATER);
    }

    @Override
    public int getTickRate(WorldView world) { return 5; }

    @Override
    protected float getBlastResistance() { return 100.0f; }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return Registration.MOONLIGHT_BLOCK.getDefaultState().with(FluidBlock.LEVEL, getBlockStateLevel(state));
    }

    @Override
    public Optional<SoundEvent> getBucketFillSound() {
        return Optional.of(SoundEvents.ITEM_BUCKET_FILL);
    }


    @Override
    public boolean isStill(FluidState state) { return false; }

    @Override
    public boolean matchesType(Fluid fluid) {
        return fluid == Registration.MOONLIGHT_FLUID || fluid == Registration.FLOWING_MOONLIGHT_FLUID;
    }

    public static class Flowing extends MoonlightFluid {
        @Override
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getLevel(FluidState state) {
            return state.get(LEVEL);
        }
    }

    public static class Still extends MoonlightFluid {
        @Override
        public int getLevel(FluidState state) {
            return 8;
        }

        @Override
        public boolean isStill(FluidState state) {
            return true;
        }
    }

}
