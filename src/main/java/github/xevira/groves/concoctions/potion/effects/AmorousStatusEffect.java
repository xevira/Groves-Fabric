package github.xevira.groves.concoctions.potion.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.List;

/**
 * <p>When applied to a player, will put random animal entities within range that can breed in the <i>mood</i>.</p>
 *
 * <p>When applied to an animal, will attempt to put them in the <i>mood</i>.</p>
 * **/
public class AmorousStatusEffect extends StatusEffectBase {
    private static final int AMOROUS_SPEED = 30;
    private static final int BASE_IN_LOVE = 600;
    private static final int LOVE_PER_LEVEL = 200;
    private static final int HORIZONTAL_RANGE = 10;
    private static final int VERTICAL_RANGE = 5;

    private static final Random RNG = Random.create();

    public AmorousStatusEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0xE0559D, 40, false);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayerEntity player)
        {
            BlockPos pos = player.getBlockPos();

            Vec3d corner1 = new Vec3d(pos.getX() - HORIZONTAL_RANGE, pos.getY() - VERTICAL_RANGE, pos.getZ() - HORIZONTAL_RANGE);
            Vec3d corner2 = new Vec3d(pos.getX() + HORIZONTAL_RANGE, pos.getY() + VERTICAL_RANGE, pos.getZ() + HORIZONTAL_RANGE);

            Box box = new Box(corner1, corner2);

            List<AnimalEntity> animals = world.getEntitiesByClass(AnimalEntity.class, box, animal -> !animal.isBaby());

            if (!animals.isEmpty())
            {
                int target = RNG.nextInt(animals.size());
                AnimalEntity animal = animals.get(target);

                makeAnimalAmorous(animal, player, amplifier);
            }
        }
        else if(entity instanceof AnimalEntity animal)
        {
            if (!animal.isBaby())
                makeAnimalAmorous(animal, null, amplifier);
        }


        return true;
    }

    private void makeAnimalAmorous(AnimalEntity animal, ServerPlayerEntity player, int amplifier)
    {
        if (animal.getBreedingAge() > 0)
        {
            // Not ready to breed yet, so let's get them closer to that
            animal.setBreedingAge(Math.max(animal.getBreedingAge() - AMOROUS_SPEED * (amplifier + 1), 0));
        }
        else if (!animal.isInLove())
        {
            animal.lovePlayer(player);

            if (animal.isInLove())
                animal.setLoveTicks(BASE_IN_LOVE + amplifier * LOVE_PER_LEVEL);
        }
    }
}
