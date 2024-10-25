package github.xevira.groves.block.entity;

import github.xevira.groves.Registration;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class MoonwellMultiblockSlaveBlockEntity extends MultiblockSlaveBlockEntity {
    public MoonwellMultiblockSlaveBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.MOONWELL_MULTIBLOCK_SLAVE_BLOCK_ENTITY, pos, state);
    }

    public MoonwellMultiblockSlaveBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public @Nullable SingleFluidStorage getFluidStorage()
    {
        if (this.world == null) return null;

        BlockPos masterPos = getMaster();
        if (masterPos == null) return null;

        if (this.world.getBlockEntity(masterPos) instanceof MoonwellMultiblockMasterBlockEntity master)
        {
            return master.getFluidStorage();
        }

        return null;
    }
}
