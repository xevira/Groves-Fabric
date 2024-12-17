package github.xevira.groves.concoctions.potion.effects;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/** Causes bees to spawn from the entity when damage is taken, similar to the Infested status effect. **/
public class SwarmingStatusEffect extends StatusEffectBase {
    public SwarmingStatusEffect() {
        super(StatusEffectCategory.NEUTRAL, 0xFFFF00, 0, false);
    }

    @Override
    public void onEntityDamage(ServerWorld world, LivingEntity entity, int amplifier, DamageSource source, float amount) {
        // TODO: Make the chance values a server configuration setting
        float chance = 0.1f + 0.05f * amplifier;
        LivingEntity attacker = null;
        if (source.getAttacker() instanceof LivingEntity living && living != entity)
            attacker = living;

        if (entity.getRandom().nextFloat() <= chance) {
            int i = MathHelper.nextBetween(entity.getRandom(), 1, 2);

            for (int j = 0; j < i; j++) {
                spawnBee(world, entity, attacker, entity.getX(), entity.getY() + (double)entity.getHeight() / 2.0, entity.getZ());
            }
        }
    }

    private void spawnBee(ServerWorld world, LivingEntity entity, @Nullable LivingEntity attacker, double x, double y, double z) {
        BeeEntity beeEntity = EntityType.BEE.create(world, SpawnReason.TRIGGERED);
        if (beeEntity != null) {
            Random random = entity.getRandom();
            float f = (float) (Math.PI / 2);
            float g = MathHelper.nextBetween(random, (float) (-Math.PI / 2), (float) (Math.PI / 2));
            Vector3f vector3f = entity.getRotationVector().toVector3f().mul(0.3F).mul(1.0F, 1.5F, 1.0F).rotateY(g);
            beeEntity.refreshPositionAndAngles(x, y, z, world.getRandom().nextFloat() * 360.0F, 0.0F);
            beeEntity.setVelocity(new Vec3d(vector3f));
            world.spawnEntity(beeEntity);
            beeEntity.playSoundIfNotSilent(SoundEvents.ENTITY_BEE_HURT);

            // Make bees made at whoever did the damage.
            if (attacker != null)
            {
                beeEntity.setAttacker(attacker);
                beeEntity.setTarget(attacker);
                beeEntity.setAngryAt(attacker.getUuid());
                beeEntity.chooseRandomAngerTime();
            }
        }
    }

}
