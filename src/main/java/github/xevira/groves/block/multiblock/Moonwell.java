package github.xevira.groves.block.multiblock;

import github.xevira.groves.Groves;
import github.xevira.groves.Registration;
import github.xevira.groves.block.entity.MoonwellMultiblockMasterBlockEntity;
import github.xevira.groves.block.entity.MoonwellMultiblockSlaveBlockEntity;
import github.xevira.groves.poi.GrovesPOI;
import github.xevira.groves.util.QuintConsumer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ReassignedVariable")
public class Moonwell {
    private static final MoonwellBlockType[][][] LAYERS;

    private static final Map<Block, Block> CONVERT = new HashMap<>();
    private static final Map<Block, Block> REVERT = new HashMap<>();

    static {
        LAYERS = new MoonwellBlockType[2][5][5];

        // Bottom Layer
        LAYERS[0][0][0] = MoonwellBlockType.BRICK;
        LAYERS[0][0][1] = MoonwellBlockType.BRICK;
        LAYERS[0][0][2] = MoonwellBlockType.BRICK;
        LAYERS[0][0][3] = MoonwellBlockType.BRICK;
        LAYERS[0][0][4] = MoonwellBlockType.BRICK;
        LAYERS[0][1][0] = MoonwellBlockType.BRICK;
        LAYERS[0][1][1] = MoonwellBlockType.BRICK;
        LAYERS[0][1][2] = MoonwellBlockType.BRICK;
        LAYERS[0][1][3] = MoonwellBlockType.BRICK;
        LAYERS[0][1][4] = MoonwellBlockType.BRICK;
        LAYERS[0][2][0] = MoonwellBlockType.BRICK;
        LAYERS[0][2][1] = MoonwellBlockType.BRICK;
        LAYERS[0][2][2] = MoonwellBlockType.CAULDRON;
        LAYERS[0][2][3] = MoonwellBlockType.BRICK;
        LAYERS[0][2][4] = MoonwellBlockType.BRICK;
        LAYERS[0][3][0] = MoonwellBlockType.BRICK;
        LAYERS[0][3][1] = MoonwellBlockType.BRICK;
        LAYERS[0][3][2] = MoonwellBlockType.BRICK;
        LAYERS[0][3][3] = MoonwellBlockType.BRICK;
        LAYERS[0][3][4] = MoonwellBlockType.BRICK;
        LAYERS[0][4][0] = MoonwellBlockType.BRICK;
        LAYERS[0][4][1] = MoonwellBlockType.BRICK;
        LAYERS[0][4][2] = MoonwellBlockType.BRICK;
        LAYERS[0][4][3] = MoonwellBlockType.BRICK;
        LAYERS[0][4][4] = MoonwellBlockType.BRICK;


        // Top Layer
        LAYERS[1][0][0] = MoonwellBlockType.BRICK;
        LAYERS[1][0][1] = MoonwellBlockType.BRICK;
        LAYERS[1][0][2] = MoonwellBlockType.BRICK;
        LAYERS[1][0][3] = MoonwellBlockType.BRICK;
        LAYERS[1][0][4] = MoonwellBlockType.BRICK;
        LAYERS[1][1][0] = MoonwellBlockType.BRICK;
        LAYERS[1][1][1] = MoonwellBlockType.AIR;
        LAYERS[1][1][2] = MoonwellBlockType.AIR;
        LAYERS[1][1][3] = MoonwellBlockType.AIR;
        LAYERS[1][1][4] = MoonwellBlockType.BRICK;
        LAYERS[1][2][0] = MoonwellBlockType.BRICK;
        LAYERS[1][2][1] = MoonwellBlockType.AIR;
        LAYERS[1][2][2] = MoonwellBlockType.AIR;
        LAYERS[1][2][3] = MoonwellBlockType.AIR;
        LAYERS[1][2][4] = MoonwellBlockType.BRICK;
        LAYERS[1][3][0] = MoonwellBlockType.BRICK;
        LAYERS[1][3][1] = MoonwellBlockType.AIR;
        LAYERS[1][3][2] = MoonwellBlockType.AIR;
        LAYERS[1][3][3] = MoonwellBlockType.AIR;
        LAYERS[1][3][4] = MoonwellBlockType.BRICK;
        LAYERS[1][4][0] = MoonwellBlockType.BRICK;
        LAYERS[1][4][1] = MoonwellBlockType.BRICK;
        LAYERS[1][4][2] = MoonwellBlockType.BRICK;
        LAYERS[1][4][3] = MoonwellBlockType.BRICK;
        LAYERS[1][4][4] = MoonwellBlockType.BRICK;

        CONVERT.put(Blocks.AIR, Registration.MOONWELL_FAKE_FLUID_BLOCK);
        CONVERT.put(Blocks.CAULDRON, Registration.MOONWELL_BASIN_BLOCK);
        CONVERT.put(Registration.CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK, Registration.CHISELED_MOONWELL_BRICKS_FULL_MOON_BLOCK);
        CONVERT.put(Registration.CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK, Registration.CHISELED_MOONWELL_BRICKS_WANING_GIBBOUS_BLOCK);
        CONVERT.put(Registration.CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK, Registration.CHISELED_MOONWELL_BRICKS_THIRD_QUARTER_BLOCK);
        CONVERT.put(Registration.CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK, Registration.CHISELED_MOONWELL_BRICKS_WANING_CRESCENT_BLOCK);
        CONVERT.put(Registration.CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK, Registration.CHISELED_MOONWELL_BRICKS_NEW_MOON_BLOCK);
        CONVERT.put(Registration.CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK, Registration.CHISELED_MOONWELL_BRICKS_WAXING_CRESCENT_BLOCK);
        CONVERT.put(Registration.CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK, Registration.CHISELED_MOONWELL_BRICKS_FIRST_QUARTER_BLOCK);
        CONVERT.put(Registration.CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK, Registration.CHISELED_MOONWELL_BRICKS_WAXING_GIBBOUS_BLOCK);
        CONVERT.put(Registration.CRACKED_MOONSTONE_BRICKS_BLOCK, Registration.CRACKED_MOONWELL_BRICKS_BLOCK);
        CONVERT.put(Registration.MOONSTONE_BRICKS_BLOCK, Registration.MOONWELL_BRICKS_BLOCK);
        CONVERT.put(Registration.MOONSTONE_BRICK_SLAB_BLOCK, Registration.MOONWELL_BRICK_SLAB_BLOCK);
        CONVERT.put(Registration.MOONSTONE_BRICK_STAIRS_BLOCK, Registration.MOONWELL_BRICK_STAIRS_BLOCK);
        CONVERT.put(Registration.MOONSTONE_BRICK_WALL_BLOCK, Registration.MOONWELL_BRICK_WALL_BLOCK);

        CONVERT.forEach((k, v) -> REVERT.put(v, k));
    }

    public static @Nullable Block convert(Block input) {
        if (CONVERT.containsKey(input))
            return CONVERT.get(input);

        return null;
    }

    public static @Nullable Block revert(Block input) {
        if (REVERT.containsKey(input))
            return REVERT.get(input);

        return null;
    }

    private static boolean isValidBlockState(BlockState state, MoonwellBlockType type)
    {
        return switch(type)
        {
            case IGNORE -> true;
            case AIR -> state.isOf(Blocks.AIR);
            case BRICK -> state.isIn(Registration.MOONWELL_CONSTRUCTION_BLOCKS);
            case CAULDRON -> state.isOf(Blocks.CAULDRON);
        };
    }

    public static boolean tryForm(PlayerEntity player, World world, BlockPos pos) {
        BlockState[][][] states = new BlockState[2][5][5];

        // Check if the blocks can be formed into a moonwell
        for (int l = 0; l < 2; l++) {
            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < 5; c++) {
                    if (LAYERS[l][r][c] == null) continue;  // Ignore this spot

                    BlockPos p = pos.add(c - 2, l, r - 2);
                    BlockState state = world.getBlockState(p);

                    if (!isValidBlockState(state, LAYERS[l][r][c])) {
                        player.sendMessage(Groves.text("text", "moonwell.invalid_block", p.getX(), p.getY(), p.getZ()), false);
                        return false;
                    }
                }
            }
        }

        // Place the blocks
        for (int l = 0; l < 2; l++) {
            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < 5; c++) {
                    if (LAYERS[l][r][c] == MoonwellBlockType.IGNORE) {
                        states[l][r][c] = null;
                        continue;  // Ignore this spot
                    }

                    BlockPos loc = pos.add(c - 2, l, r - 2);

                    BlockState state = world.getBlockState(loc);

                    if (CONVERT.containsKey(state.getBlock())) {
                        Block to = CONVERT.get(state.getBlock());

                        world.setBlockState(loc, to.getDefaultState());
                    }

                    states[l][r][c] = state;
                }
            }
        }

        if (world.getBlockEntity(pos) instanceof MoonwellMultiblockMasterBlockEntity masterBlockEntity) {
            masterBlockEntity.setOriginalState(states[0][2][2]);
            // Link the Block Entities
            for (int l = 0; l < 2; l++) {
                for (int r = 0; r < 5; r++) {
                    for (int c = 0; c < 5; c++) {
                        if (LAYERS[l][r][c] == MoonwellBlockType.IGNORE) continue;  // Ignore this spot

                        BlockPos loc = pos.add(c - 2, l, r - 2);

                        BlockEntity blockEntity = world.getBlockEntity(loc);
                        if (blockEntity instanceof MoonwellMultiblockSlaveBlockEntity slaveBlockEntity) {
                            masterBlockEntity.addSlave(slaveBlockEntity, states[l][r][c]);
                        }
                    }
                }
            }

            // Mark completed and set owner
            masterBlockEntity.markFormed();
            masterBlockEntity.setOwner(player.getUuid());
            return true;
        }


        return false;
    }

    private static @Nullable BlockPos getMasterPos(ServerWorld world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof MoonwellMultiblockMasterBlockEntity) {
            //Groves.LOGGER.info("getMasterPos: pos = {}", pos);
            return pos; // Already at the master block
        }

        if (blockEntity instanceof MoonwellMultiblockSlaveBlockEntity slave) {
            BlockPos pos2 = slave.getMaster();

            if (pos2 != null) {
                // Verify the slave isn't messed up...
                blockEntity = world.getBlockEntity(pos2);
                if (blockEntity instanceof MoonwellMultiblockMasterBlockEntity) {
                    //Groves.LOGGER.info("getMasterPos: pos2 = {}", pos2);
                    return pos2;
                }
            }
        }

        return null;
    }

    public static void randomConvert(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        // Dimension has no time
        if (world.getDimension().hasFixedTime()) return;

        // Dimension is day time
        if (world.isDay()) return;

        List<BlockPos> moonwellMasters = new ArrayList<>();
        Block conversion = Moonwell.convert(state.getBlock());
        if (conversion == null) {
            Groves.LOGGER.info("randomConvert({}) - {} has no conversion block", pos, state.getBlock());
            return;  // Nothing to do
        }

        // Look for blocks that are part of a moonwell
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    BlockPos neighborPos = pos.add(x, y, z);
                    BlockState neighbor = world.getBlockState(neighborPos);

                    if (neighbor.isIn(Registration.MOONWELL_BLOCKS)) {
                        BlockPos masterPos = getMasterPos(world, neighborPos);
                        if (masterPos != null && masterPos.getSquaredDistance(pos) <= 100 /* 10 block radius out */)
                            moonwellMasters.add(masterPos);
                    }
                }
            }
        }

        if (!moonwellMasters.isEmpty()) {
            BlockPos masterPos = (moonwellMasters.size() > 1) ?
                    moonwellMasters.get(random.nextInt(moonwellMasters.size())) :
                    moonwellMasters.getFirst();

            //Groves.LOGGER.info("randomTick: Converting to Moonwell Bricks at {} with {}", pos, masterPos);

            if (world.getBlockEntity(masterPos) instanceof MoonwellMultiblockMasterBlockEntity masterBlockEntity) {
                //Groves.LOGGER.info("randomTick: Converting to Moonwell Bricks at {} with {}", pos, masterPos);
                // TODO: Does the moonwell have enough power to do so?

                BlockState newState = conversion.getStateWithProperties(state);
                //Groves.LOGGER.info("randomTick: newState = {}", newState);

                world.setBlockState(pos, newState); // Change the block
                if (world.getBlockEntity(pos) instanceof MoonwellMultiblockSlaveBlockEntity slaveBlockEntity) {
                    masterBlockEntity.addSlave(slaveBlockEntity, state);
                }

            }
        }
    }

    public static void randomRevert(BlockState state, ServerWorld world, BlockPos pos, Random random) {

        if (world.getBlockEntity(pos) instanceof MoonwellMultiblockSlaveBlockEntity slaveBlockEntity) {

            if (!slaveBlockEntity.isDecorative()) return;

            boolean connected = false;
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;

                        BlockPos neighborPos = pos.add(x, y, z);
                        BlockState neighbor = world.getBlockState(neighborPos);

                        if (neighbor.isIn(Registration.MOONWELL_BLOCKS)) {
                            connected = true;
                        }
                    }
                }
            }

            if (!connected) {

                BlockPos masterPos = slaveBlockEntity.getMaster();
                if (masterPos != null)
                {
                    if (world.getBlockEntity(masterPos) instanceof MoonwellMultiblockMasterBlockEntity masterBlockEntity)
                    {
                        masterBlockEntity.breakSlaveBlock(pos);
                        masterBlockEntity.removeDecoration(pos);
                    }
                }
            }
        }
    }

    public static void breakSlaveBlock(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved, QuintConsumer<BlockState, World, BlockPos, BlockState, Boolean> superOnStateChanged) {
        if (!moved && !state.isOf(newState.getBlock()))
        {
            if (world.getBlockEntity(pos) instanceof MoonwellMultiblockSlaveBlockEntity slaveBlockEntity)
            {
                // Get the master position *first*
                BlockPos masterPos = slaveBlockEntity.getMaster();

                superOnStateChanged.accept(state, world, pos, newState, moved);

                if (masterPos != null)
                {
                    if (world.getBlockEntity(masterPos) instanceof MoonwellMultiblockMasterBlockEntity masterBlockEntity)
                    {
                        if (slaveBlockEntity.isDecorative())
                            masterBlockEntity.removeDecoration(pos);
                        else
                            masterBlockEntity.breakMultiblock(false);
                    }
                }
            }
            else
            {
                superOnStateChanged.accept(state, world, pos, newState, moved);
            }
        }
    }

    private static boolean tryOpenScreen(MoonwellMultiblockMasterBlockEntity master, PlayerEntity player)
    {
        player.openHandledScreen(master);
        return true;
    }

    public static void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit)
    {
        if (!world.isClient) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof MoonwellMultiblockMasterBlockEntity master) {
                if (master.canInteract(player)) {
                    player.openHandledScreen(master);
                }

            } else if (blockEntity instanceof MoonwellMultiblockSlaveBlockEntity slave) {
                BlockPos masterPos = slave.getMaster();

                if (masterPos != null && world.getBlockEntity(masterPos) instanceof MoonwellMultiblockMasterBlockEntity master) {
                    if (master.canInteract(player)) {
                        player.openHandledScreen(master);
                    }
                }
            }
        }
    }

    public enum MoonwellBlockType
    {
        IGNORE,
        AIR,
        BRICK,
        CAULDRON;
    }
}
