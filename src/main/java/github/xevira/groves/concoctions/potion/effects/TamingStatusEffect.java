package github.xevira.groves.concoctions.potion.effects;

import github.xevira.groves.Groves;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.List;

/** Tames random nearby tamable entities without needing what tames them.  Will cause them to sit, if possible. **/
public class TamingStatusEffect extends StatusEffectBase {
    private static final Random RNG = Random.create();

    public TamingStatusEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0xcc2200, 100, false);
    }


    private void TameAnimal(TameableEntity tamable, ServerPlayerEntity player)
    {
        // Check if the target is angered first.  If so, calm it.
        if (tamable instanceof Angerable anger)
        {
            if (anger.hasAngerTime())
            {
                Groves.LOGGER.info("Quelling Anger for {} at {}", tamable.getType().getTranslationKey(), tamable.getPos());
                anger.stopAnger();
                return;
            }
        }

        if (!tamable.isTamed())
        {
            tamable.setOwner(player);
            tamable.setSitting(true);

            // TODO: Check if more is needed
            Groves.LOGGER.info("Taming {} at {}", tamable.getType().getTranslationKey(), tamable.getPos());
        }
    }

    private void TameAnimals(ServerPlayerEntity player, int amplifier)
    {
        int w = 5 + 3 * amplifier;
        int h = 2 + amplifier;

        BlockPos pos = player.getBlockPos();
        Vec3d corner1 = new Vec3d(pos.getX() - w, pos.getY() - h, pos.getZ() - w);
        Vec3d corner2 = new Vec3d(pos.getX() + w, pos.getY() + h, pos.getZ() + w);

        Box box = new Box(corner1, corner2);

        Groves.LOGGER.info("Taming: box = {}", box);
        List<TameableEntity> tamables = player.getWorld().getEntitiesByClass(TameableEntity.class, box, tamable -> !tamable.isTamed());

        Groves.LOGGER.info("Taming: tamables.size() = {}", tamables.size());

        for(TameableEntity tamable : tamables)
        {
            if (canTame(amplifier))
                TameAnimal(tamable, player);
        }
    }

    private boolean canTame(int amplifier)
    {
        double chance = 0.5D + 0.1D * amplifier;
        return RNG.nextDouble() < chance;
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayerEntity player)
        {
            TameAnimals(player, amplifier);
        }

        // Does nothing on any other entity.

        return true;
    }


}
