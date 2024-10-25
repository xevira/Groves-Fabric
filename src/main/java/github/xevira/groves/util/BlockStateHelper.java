package github.xevira.groves.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;

public class BlockStateHelper {
    private static <T extends Comparable<T>> BlockState copyProperty(BlockState from, BlockState to, Property<T> property) {
        return to.withIfExists(property, from.get(property));
    }

    public static BlockState copyProperties(BlockState source, BlockState target) {
        for (Property<?> prop : source.getProperties()) {
            target = copyProperty(source, target, prop);
        }

        return target;
    }
}
