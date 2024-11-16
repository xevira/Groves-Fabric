package github.xevira.groves.worldgen.foliage;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.xevira.groves.Registration;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.BlobFoliagePlacer;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.foliage.FoliagePlacerType;

public class SanctumFoliagePlacer extends BlobFoliagePlacer {
    public static final MapCodec<SanctumFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(
            instance -> createCodec(instance).apply(instance, SanctumFoliagePlacer::new)
    );

    public SanctumFoliagePlacer(IntProvider radius, IntProvider offset, int height) {
        super(radius, offset, height);
    }

    @Override
    protected FoliagePlacerType<?> getType() {
        return Registration.SANCTUM_FOLIAGE_PLACER;
    }

    @Override
    protected void generate(
            TestableWorld world,
            FoliagePlacer.BlockPlacer placer,
            Random random,
            TreeFeatureConfig config,
            int trunkHeight,
            FoliagePlacer.TreeNode treeNode,
            int foliageHeight,
            int radius,
            int offset
    ) {
        boolean bl = treeNode.isGiantTrunk();
        BlockPos blockPos = treeNode.getCenter().up(offset);
        int i = radius + treeNode.getFoliageRadius() - 1;
        this.generateSquare(world, placer, random, config, blockPos, i - 2, foliageHeight - 3, bl);
        this.generateSquare(world, placer, random, config, blockPos, i - 1, foliageHeight - 4, bl);

        for (int j = foliageHeight - 5; j >= 0; j--) {
            this.generateSquare(world, placer, random, config, blockPos, i, j, bl);
        }
    }

    @Override
    protected boolean isInvalidForLeaves(Random random, int dx, int y, int dz, int radius, boolean giantTrunk) {
        return MathHelper.square((float)dx + 0.5F) + MathHelper.square((float)dz + 0.5F) > (float)(radius * radius);
    }
}
