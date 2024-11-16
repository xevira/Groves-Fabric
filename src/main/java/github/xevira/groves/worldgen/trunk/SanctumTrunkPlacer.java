package github.xevira.groves.worldgen.trunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.xevira.groves.Registration;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.trunk.CherryTrunkPlacer;
import net.minecraft.world.gen.trunk.TrunkPlacer;
import net.minecraft.world.gen.trunk.TrunkPlacerType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class SanctumTrunkPlacer extends TrunkPlacer {
    private static final Codec<UniformIntProvider> BRANCH_START_OFFSET_FROM_TOP_CODEC = UniformIntProvider.CODEC
            .codec()
            .validate(
                    branchStartOffsetFromTop -> branchStartOffsetFromTop.getMax() - branchStartOffsetFromTop.getMin() < 1
                            ? DataResult.error(() -> "Need at least 2 blocks variation for the branch starts to fit both branches")
                            : DataResult.success(branchStartOffsetFromTop)
            );

    public static final MapCodec<SanctumTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(
            instance -> fillTrunkPlacerFields(instance)
                    .<IntProvider, IntProvider, IntProvider>and(
                            instance.group(
                                    IntProvider.createValidatingCodec(3, 5).fieldOf("tree_sections").forGetter(trunkPlacer -> trunkPlacer.treeSections),
                                    IntProvider.createValidatingCodec(3, 6).fieldOf("branch_count").forGetter(trunkPlacer -> trunkPlacer.branchCount),
                                    IntProvider.createValidatingCodec(12, 16).fieldOf("branch_horizontal_length").forGetter(trunkPlacer -> trunkPlacer.branchHorizontalLength)
                            )
                    )
                    .apply(instance, SanctumTrunkPlacer::new)
    );


    private final IntProvider branchCount;
    private final IntProvider branchHorizontalLength;
    private final IntProvider treeSections;

    /** Will not generate with a CORE. **/
    public SanctumTrunkPlacer(
            int baseHeight,
            int firstRandomHeight,
            int secondRandomHeight,
            IntProvider treeSections,
            IntProvider branchCount,
            IntProvider branchHorizontalLength
    ) {
        super(baseHeight, firstRandomHeight, secondRandomHeight);
        this.treeSections = treeSections;
        this.branchCount = branchCount;
        this.branchHorizontalLength = branchHorizontalLength;
    }

    @Override
    protected TrunkPlacerType<?> getType() {
        return Registration.SANCTUM_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.TreeNode> generate(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, int height, BlockPos startPos, TreeFeatureConfig config) {
        // First, determine how many sections the tree will have
        int sections = this.treeSections.get(random);

        int sectionHeight = (height + sections - 1) / sections;

        // Generate dirt under the tree base
        generateDirt(world, replacer, random, sections - 1, startPos, config);

        List<FoliagePlacer.TreeNode> list = new ArrayList<>();
        int y = 0;

        for(int section = sections; section > 0; section--)
        {
            double scale = Math.sqrt(MathHelper.map(section, sections, 1, 1.0, 0.0));
            int bottomSize = section - 1; // How wide is the trunk
            int topSize = Math.max(0, section - 2);

            // Generate a tapered trunk section
            generateTrunk(world, replacer, random, sectionHeight, bottomSize, topSize, startPos.up(y), config);

            y += sectionHeight;

            if (scale > 0.0 && section > 1) {

                int branches = this.branchCount.get(random);
                double wedge = 0.5 * Math.PI / branches;
                double branchLength = branchHorizontalLength.get(random) * scale;

                for (int branch = 0; branch < branches; branch++) {
                    double angle = (random.nextDouble() + 4.0 * branch) * wedge;

                    // Make branch out the angle from the trunk
                    list.addAll(generateBranch(world, replacer, random, branchLength, 0.0, 0.5, 0.75, 3.0, angle, 2.1, startPos.up(y), config));
                }
            }
        }

        // Foliage clump at the top of the tree
        list.add(new FoliagePlacer.TreeNode(startPos.up(height), 4, false));

        return list;
    }

    private void generateDirt(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, int size, BlockPos startPos, TreeFeatureConfig config)
    {
        double sizeSq = size * size;

        for(int x = -size; x <= size; x++)
        {
            for(int z = -size; z <= size; z++)
            {
                double distSq = x * x + z * z;
                if (distSq <= sizeSq)
                {
                    BlockPos pos = startPos.add(x, -1, z);
                    setToDirt(world, replacer, random, pos, config);
                }
            }
        }
    }


    private void generateTrunk(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, int height, int startSize, int endSize, BlockPos startPos, TreeFeatureConfig config)
    {
        for(int y = 0; y < height; y++) {
            double size = MathHelper.map(y, 0, height - 1, startSize, endSize);
            double sizeSq = size * size;

            int sz = MathHelper.floor(size);

            for(int x = -sz; x <= sz; x++)
            {
                for(int z = -sz; z <= sz; z++)
                {
                    double distSq = x * x + z * z;
                    if (distSq <= sizeSq)
                    {
                        BlockPos pos = startPos.add(x, y, z);
                        getAndSetState(world, replacer, random, pos, config);
                    }
                }
            }
        }
    }

    private int getLongestSide(BlockPos offset) {
        int i = MathHelper.abs(offset.getX());
        int j = MathHelper.abs(offset.getY());
        int k = MathHelper.abs(offset.getZ());
        return Math.max(i, Math.max(j, k));
    }

    private Direction.Axis getLogAxis(BlockPos branchStart, BlockPos branchEnd) {
        Direction.Axis axis = Direction.Axis.Y;
        int i = Math.abs(branchEnd.getX() - branchStart.getX());
        int j = Math.abs(branchEnd.getZ() - branchStart.getZ());
        int k = Math.max(i, j);
        if (k > 0) {
            if (i == k) {
                axis = Direction.Axis.X;
            } else {
                axis = Direction.Axis.Z;
            }
        }

        return axis;
    }

    private List<FoliagePlacer.TreeNode> generateBranch(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, double max_length, double progress, double minBranch, double maxBranch, double minLength, double angle, double branchChance, BlockPos startPos, TreeFeatureConfig config)
    {
        List<FoliagePlacer.TreeNode> nodes = new ArrayList<>();

        double bz = Math.cos(angle);
        double bx = Math.sin(angle);

        double left = 1.0 - progress;

        double p = (random.nextDouble() * (maxBranch - minBranch) + minBranch) * left;


        boolean willBranch = random.nextDouble() < branchChance;
        branchChance -= 1.0;

        // Determine if the remaining length is below the min length for branching
        if (!willBranch || (max_length * left) <= minLength)
        {
            p = left;   // Use up remaining branch
        }

        double length = p * max_length;

        // Generate branch from startpos out angle
        // Every branch goes up one block from start to end
        BlockPos delta = new BlockPos(MathHelper.floor(bx * length), 1, MathHelper.floor(bz * length));
        BlockPos end = startPos.add(delta);
        int major = getLongestSide(delta);
        double dx = (double)delta.getX() / (double)major;
        double dy = (double)delta.getY() / (double)major;
        double dz = (double)delta.getZ() / (double)major;

        for(int m = 0; m <= major; m++) {
            BlockPos pos = startPos.add(MathHelper.floor(0.5 + m * dx), MathHelper.floor(0.5 + m * dy), MathHelper.floor(0.5 + m * dz));
            this.getAndSetState(world, replacer, random, pos, config, state -> state.withIfExists(PillarBlock.AXIS, this.getLogAxis(startPos, pos)));
        }

        progress += p;

        int foliageRadius = (int)MathHelper.map(progress, 0.5, 1.0, 0, 4);

        if (foliageRadius > 0)
        {
            // Add foliage node when beyond a certain distance along the branch
            nodes.add(new FoliagePlacer.TreeNode(end, foliageRadius, false));
        }

        if (progress < 1.0) {

            // Create forking branches
            int branches = this.branchCount.get(random) + 1;

            double sweep = random.nextDouble() * 0.5 * (1.0 - progress);
            double startAngle = angle - 0.5 * sweep * random.nextDouble();
            double endAngle = startAngle + sweep;

            double wedge = sweep / (branches - 1);

            for (int branch = 0; branch < branches; branch++) {
                double new_angle = startAngle + wedge * branch;

                nodes.addAll(generateBranch(world, replacer, random, max_length, progress, minBranch, maxBranch, minLength, new_angle, branchChance, end, config));
            }
        }

        return nodes;
    }
}
