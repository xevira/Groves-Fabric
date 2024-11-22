package github.xevira.groves.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.xevira.groves.Registration;
import github.xevira.groves.ServerConfig;
import github.xevira.groves.poi.WindChimes;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import net.minecraft.world.tick.TickPriority;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class WindChimeBlock extends Block {
    public static final MapCodec<WindChimeBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.intRange(0, 100).fieldOf("condition").forGetter(WindChimeBlock::getCondition),
                    createSettingsCodec()
            ).apply(instance, WindChimeBlock::new)
    );

    public static final Map<Integer, VoxelShape> SHAPES = new HashMap<>()
    {{
        put(100, VoxelShapes.union(
                Block.createCuboidShape(0.0, 11.0, 0.0, 16.0, 12.0, 16.0),
                Block.createCuboidShape(6.5, 12.0, 6.5, 9.5, 16.0, 9.5),
                Block.createCuboidShape(2.0, 0.0, 2.0, 5.0, 11.0, 5.0),
                Block.createCuboidShape(11.0, -5.0, 2.0, 14.0, 11.0, 5.0),
                Block.createCuboidShape(11.0, -7.0, 11.0, 14.0, 11.0, 14.0),
                Block.createCuboidShape(2.0, -3.0, 11.0, 5.0, 11.0, 14.0)
        ).simplify());
        put(50, VoxelShapes.union(
                Block.createCuboidShape(0.0, 11.0, 0.0, 16.0, 12.0, 16.0),
                Block.createCuboidShape(6.5, 12.0, 6.5, 9.5, 16.0, 9.5),
                Block.createCuboidShape(2.0, 0.0, 2.0, 5.0, 11.0, 5.0),
                Block.createCuboidShape(11.0, -7.0, 11.0, 14.0, 11.0, 14.0),
                Block.createCuboidShape(2.0, -3.0, 11.0, 5.0, 11.0, 14.0)
        ).simplify());
        put(0, VoxelShapes.union(
                Block.createCuboidShape(0.0, 11.0, 0.0, 16.0, 12.0, 16.0),
                Block.createCuboidShape(6.5, 12.0, 6.5, 9.5, 16.0, 9.5),
                Block.createCuboidShape(2.0, 0.0, 2.0, 5.0, 11.0, 5.0),
                Block.createCuboidShape(11.0, -7.0, 11.0, 14.0, 11.0, 14.0)
        ).simplify());
    }};

    public static final VoxelShape COLLIDE_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(0.0, 11.0, 0.0, 16.0, 12.0, 16.0),
            Block.createCuboidShape(6.5, 12.0, 6.5, 9.5, 16.0, 9.5)
    ).simplify();

    public static final BooleanProperty CHIMING = BooleanProperty.of("chiming");

    private final int condition;

    public WindChimeBlock(int condition, Settings settings) {
        super(settings);

        this.condition = condition;
        setDefaultState(this.getStateManager().getDefaultState().with(CHIMING, false));
    }

    public int getCondition() { return this.condition; }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES.get(this.condition);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return COLLIDE_SHAPE;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return Block.sideCoversSmallSquare(world, pos.offset(Direction.UP), Direction.DOWN);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        WindChimes.addChime(world, pos);
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!moved && !(newState.getBlock() instanceof WindChimeBlock))
        {
            WindChimes.removeChime(world, pos);
        }

        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        return direction == Direction.UP && !state.canPlaceAt(world, pos)
                ? Blocks.AIR.getDefaultState()
                : super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world instanceof ServerWorld serverWorld) {
            if (WindChimeBlock.chimes(serverWorld, pos)) {
                world.playSound(null, pos, Registration.WIND_CHIME_COLLIDE_SOUND, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
        }
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) { return true; }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.setBlockState(pos, state.with(CHIMING, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(CHIMING);
    }

    public static boolean chimes(ServerWorld world, BlockPos pos)
    {
        BlockState state = world.getBlockState(pos);
        if (!state.get(CHIMING)) {
            world.setBlockState(pos, state.with(CHIMING, true));
            world.scheduleBlockTick(pos, state.getBlock(), world.random.nextBetween(10, 50), TickPriority.LOW);
            return true;
        }
        else if (!world.getBlockTickScheduler().isTicking(pos, state.getBlock()))
            world.scheduleBlockTick(pos, state.getBlock(), world.random.nextBetween(10, 50), TickPriority.LOW);
        return false;
    }

    public static void protect(ServerWorld world, BlockPos pos)
    {
        if (chimes(world, pos))
        {
            world.playSound(null, pos, Registration.WIND_CHIME_PROTECT_SOUND, SoundCategory.BLOCKS, 1.0f, 1.0f);

            if (world.random.nextFloat() < ServerConfig.getWindChimeChanceDamage())
            {
                BlockState state = world.getBlockState(pos);
                Block block = state.getBlock();

                if (block instanceof WindChimeBlock chime)
                {
                    if (chime.condition <= 0)
                        world.breakBlock(pos, false);
                    else if (chime.condition <= 50) {
                        world.setBlockState(pos, Registration.DAMAGED_WIND_CHIME_BLOCK.getDefaultState().with(CHIMING, true));
                        world.playSound(null, pos, Registration.WIND_CHIME_BREAK_SOUND, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    }
                    else if (chime.condition <= 100) {
                        world.setBlockState(pos, Registration.WORN_WIND_CHIME_BLOCK.getDefaultState().with(CHIMING, true));
                        world.playSound(null, pos, Registration.WIND_CHIME_BREAK_SOUND, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    }
                }
            }
        }
    }
}
