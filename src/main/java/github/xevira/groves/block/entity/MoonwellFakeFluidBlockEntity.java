package github.xevira.groves.block.entity;

import github.xevira.groves.Registration;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class MoonwellFakeFluidBlockEntity extends MoonwellMultiblockSlaveBlockEntity {
    public MoonwellFakeFluidBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.MOONWELL_FAKE_FLUID_BLOCK_ENTITY, pos, state);
    }
}
