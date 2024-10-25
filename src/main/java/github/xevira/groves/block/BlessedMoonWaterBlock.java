package github.xevira.groves.block;

import github.xevira.groves.Groves;
import github.xevira.groves.Registration;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BlessedMoonWaterBlock extends FluidBlock {

    public BlessedMoonWaterBlock(FlowableFluid fluid, Settings settings) {
        super(fluid, settings);
    }

    private static Optional<Item> getBlessedBlock(Item input)
    {
        return Optional.ofNullable(Registration.BLOCK_TO_BLESSED.get(input));
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient()) {
            if (entity instanceof ItemEntity itemEntity) {
                ItemStack itemStack = itemEntity.getStack();

                Optional<Item> optional = getBlessedBlock(itemStack.getItem());

                if (optional.isPresent()) {
                    // TODO: Play Sound

                    ItemStack blessed = itemStack.copyComponentsToNewStack(optional.get(), itemStack.getCount());
                    itemEntity.setStack(blessed);

                    // TODO: Have a chance to change the fluid block into water
                }
            }
        }
    }
}
