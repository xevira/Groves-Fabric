package github.xevira.groves.util;

import github.xevira.groves.block.entity.MultiblockMasterBlockEntity;
import github.xevira.groves.block.entity.MultiblockSlaveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public interface OwnedBlock {

    default boolean canBreakBlock(@NotNull PlayerEntity player, @NotNull World world, BlockPos pos)
    {
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof OwnableBlockEntity ownable)
        {
            return ownable.canBreak(player);
        }

        if (blockEntity instanceof MultiblockSlaveBlockEntity slave)
        {
            BlockPos masterPos = slave.getMaster();
            if (masterPos != null && world.getBlockEntity(masterPos) instanceof MultiblockMasterBlockEntity master)
            {
                return master.canBreak(player);
            }
        }

        return true;
    }

    default boolean canInteract(@NotNull PlayerEntity player, @NotNull World world, BlockPos pos)
    {
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof OwnableBlockEntity ownable)
        {
            return ownable.canInteract(player);
        }

        if (blockEntity instanceof MultiblockSlaveBlockEntity slave)
        {
            BlockPos masterPos = slave.getMaster();
            if (masterPos != null && world.getBlockEntity(masterPos) instanceof MultiblockMasterBlockEntity master)
            {
                return master.canInteract(player);
            }
        }

        return true;
    }
}
