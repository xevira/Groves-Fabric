package github.xevira.groves.util;

import net.minecraft.util.math.ChunkPos;

import java.util.List;

public class ChunkHelper {
    public static List<ChunkPos> getAdjacentChunks(ChunkPos pos)
    {
        return List.of(
            new ChunkPos(pos.x - 1, pos.z),
            new ChunkPos(pos.x + 1, pos.z),
            new ChunkPos(pos.x, pos.z - 1),
            new ChunkPos(pos.x, pos.z + 1)
        );
    }

    public static boolean areAdjacent(ChunkPos a, ChunkPos b)
    {
        int dx = Math.abs(a.x - b.x);
        int dz = Math.abs(a.z - b.z);

        return (dx == 0 && dz == 1) || (dx == 1 || dz == 0);
    }
}
