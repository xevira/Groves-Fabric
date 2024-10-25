package github.xevira.groves.util;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.world.World;

public interface ServerTickableBlockEntity {
    void serverTick();

    static <T extends BlockEntity> BlockEntityTicker<T> getTicker(World pWorld) {
        return pWorld.isClient ? null : (world, pos, state, blockEntity) -> {
            if (blockEntity instanceof ServerTickableBlockEntity tickableBlockEntity) {
                tickableBlockEntity.serverTick();
            }
        };
    }
}