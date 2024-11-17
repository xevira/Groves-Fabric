package github.xevira.groves.block;

import github.xevira.groves.Groves;
import github.xevira.groves.Registration;
import github.xevira.groves.poi.GrovesPOI;
import net.minecraft.block.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;

public class SanctumSaplingBlock extends SaplingBlock {
    public SanctumSaplingBlock(SaplingGenerator generator, Settings settings) {
        super(generator, settings);
    }

    @Override
    public void generate(ServerWorld world, BlockPos pos, BlockState state, Random random) {
        if (state.get(STAGE) == 0) {
            world.setBlockState(pos, state.cycle(STAGE), Block.NO_REDRAW);
        } else if (GrovesPOI.sanctuaryExists(world, new ChunkPos(pos))) {
            // Require 3x3 saplings
            generate3x3(world, world.getChunkManager().getChunkGenerator(), pos, state, random);
        }
    }

    public static void generate3x3(ServerWorld world, ChunkGenerator chunkGenerator, BlockPos pos, BlockState state, Random random) {
        RegistryKey<ConfiguredFeature<?, ?>> registryKey = Registration.SANCTUM_TREE_CONFIG_KEY;
        if (registryKey != null) {
            RegistryEntry<ConfiguredFeature<?, ?>> registryEntry = world.getRegistryManager()
                    .getOrThrow(RegistryKeys.CONFIGURED_FEATURE)
                    .getOptional(registryKey)
                    .orElse(null);
            if (registryEntry != null) {
                for (int i = 1; i >= -1; i--) {
                    for (int j = 1; j >= -1; j--) {
                        if (canGenerateLargeTree(state, world, pos, i, j)) {
                            ConfiguredFeature<?, ?> configuredFeature = registryEntry.value();
                            BlockState blockState = Blocks.AIR.getDefaultState();
                            setSaplings(world, pos, i, j, blockState);
                            if (configuredFeature.generate(world, chunkGenerator, random, pos.add(i, 0, j))) {
                                return;
                            }

                            // Failed to generate tree
                            setSaplings(world, pos, i, j, state);
                        }
                    }
                }
            }
        }
    }

    private static boolean canGenerateLargeTree(BlockState state, ServerWorld world, BlockPos pos, int x, int z) {
        Block block = state.getBlock();
        for(int dx = -1; dx <= 1; dx++)
        {
            for(int dz = -1; dz <= 1; dz++)
            {
                if (!world.getBlockState(pos.add(x + dx, 0, z + dz)).isOf(block)) return false;
            }
        }

        return true;
    }

    private static void setSaplings(ServerWorld world, BlockPos pos, int x, int z, BlockState state)
    {
        for(int dx = -1; dx <= 1; dx++)
        {
            for(int dz = -1; dz <= 1; dz++)
            {
                world.setBlockState(pos.add(x + dx, 0, z + dz), state, Block.NO_REDRAW);
            }
        }
    }
}
