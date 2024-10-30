package github.xevira.groves.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BlockHelper {
    public static BlockPos Vec3dtoBlockPos(Vec3d v)
    {
        return new BlockPos(MathHelper.floor(v.getX()), MathHelper.floor(v.getY()), MathHelper.floor(v.getZ()));
    }
}
