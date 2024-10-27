package github.xevira.groves.block;

import github.xevira.groves.block.multiblock.Moonwell;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

import java.util.*;
import java.util.function.Supplier;

public class MoonstoneBrickBlock extends Block {
    public MoonstoneBrickBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return true;
    }


    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        Moonwell.randomConvert(state, world, pos, random);
    }
}
