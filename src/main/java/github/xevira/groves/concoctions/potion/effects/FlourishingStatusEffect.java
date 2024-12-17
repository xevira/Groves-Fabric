package github.xevira.groves.concoctions.potion.effects;

import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.List;

/** Randomly triggers the grow ticks for nearby crops and ages nearby baby animals **/
public class FlourishingStatusEffect extends StatusEffectBase {
    private static final int ANIMAL_GROWTH = 400;    // Seconds
    private static final int HORIZONTAL_RANGE = 5;
    private static final int VERTICAL_RANGE = 2;
    private static final Box BOUNDING = new Box(-HORIZONTAL_RANGE, -VERTICAL_RANGE, -HORIZONTAL_RANGE, HORIZONTAL_RANGE, VERTICAL_RANGE, HORIZONTAL_RANGE);

    public FlourishingStatusEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0x00E696, 100, true);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        if (entity instanceof PlayerEntity player)
        {
            // Check for blocks to bone meal
            // ----------------------------------------------------
            BlockPos pos = player.getBlockPos();

            BlockPos minPos = pos.add(-HORIZONTAL_RANGE, -VERTICAL_RANGE, -HORIZONTAL_RANGE);
            BlockPos maxPos = pos.add(HORIZONTAL_RANGE, VERTICAL_RANGE, HORIZONTAL_RANGE);


            for(int y = minPos.getY(); y <= maxPos.getY(); y++)
            {
                for(int z = minPos.getZ(); z <= maxPos.getZ(); z++)
                {
                    for(int x = minPos.getX(); x <= maxPos.getX(); x++)
                    {
                        BlockPos blockPos = new BlockPos(x, y, z);
                        BlockState state = world.getBlockState(blockPos);
                        Block block = state.getBlock();

                        if (canBonemeal(block))
                        {
                            Fertilizable fertilizable = (Fertilizable) block;

                            if (fertilizable.canGrow(world, world.random, blockPos, state) &&
                                    fertilizable.isFertilizable(world, blockPos, state))
                            {
                                fertilizable.grow(world, world.random, blockPos, state);
                            }
                        }
                    }
                }
            }
            // ----------------------------------------------------

            // Check for baby animals
            // ----------------------------------------------------
            Box box = BOUNDING.offset(pos);

            List<AnimalEntity> animals = world.getEntitiesByClass(AnimalEntity.class, box, AnimalEntity::isBaby);

            for(AnimalEntity animal : animals)
                animal.setBreedingAge(Math.min(animal.getBreedingAge() + ANIMAL_GROWTH, 0));
            // ----------------------------------------------------

        }
        else if (entity instanceof AnimalEntity animal)
        {
            if (animal.isBaby())
                animal.setBreedingAge(Math.min(animal.getBreedingAge() + ANIMAL_GROWTH, 0));
        }

        return true;
    }

    private boolean canBonemeal(Block block)
    {
        // Can't be fertilized
        if (!(block instanceof Fertilizable)) return false;

        // Explicit blacklist
        return switch (block) {
            case SpreadableBlock spreadableBlock -> false;
            case NetherrackBlock netherrackBlock -> false;
            case NyliumBlock nyliumBlock -> false;
            case SeagrassBlock seagrassBlock -> false;
            case TallPlantBlock tallPlantBlock -> false;
            default -> true;
        };
    }
}
