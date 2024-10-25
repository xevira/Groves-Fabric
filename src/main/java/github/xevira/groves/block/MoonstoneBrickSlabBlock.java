package github.xevira.groves.block;

import github.xevira.groves.Registration;
import github.xevira.groves.block.entity.MoonwellMultiblockMasterBlockEntity;
import github.xevira.groves.block.entity.MoonwellMultiblockSlaveBlockEntity;
import github.xevira.groves.block.multiblock.Moonwell;
import github.xevira.groves.util.WaxHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MoonstoneBrickSlabBlock extends SlabBlock {
    public MoonstoneBrickSlabBlock(Settings settings) {
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
