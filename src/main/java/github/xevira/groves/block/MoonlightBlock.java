package github.xevira.groves.block;

import github.xevira.groves.Registration;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public class MoonlightBlock extends FluidBlock {

    public MoonlightBlock(FlowableFluid fluid, Settings settings) {
        super(fluid, settings);
    }
}
