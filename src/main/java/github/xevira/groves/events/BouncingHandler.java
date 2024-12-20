/*
 * Credit to Tinkers Construct by SlimeKnight for the algorithm.
 *
 * Had to put the functionality to work with Fabric.
 */

package github.xevira.groves.events;

import github.xevira.groves.Groves;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.IdentityHashMap;
import java.util.List;

public class BouncingHandler {
    private static final IdentityHashMap<LivingEntity, BouncingHandler> HANDLERS = new IdentityHashMap<>();

    private final LivingEntity entity;
    private int timer;
    private boolean wasInAir;
    private double bounce;
    private int bounceAge;

    private double lastVelX;
    private double lastVelZ;

    public BouncingHandler(LivingEntity entity, double bounce)
    {
        this.entity = entity;
        this.timer = 0;
        this.wasInAir = false;
        this.bounce = bounce;

        if (bounce != 0)
            this.bounceAge = entity.age + 1;    // Because the endTick handler is called *after* the age is incremented
        else
            this.bounceAge = 0;

        this.lastVelX = this.entity.getVelocity().x;
        this.lastVelZ = this.entity.getVelocity().z;
    }

    public void endTick()
    {
        boolean isRemote = this.entity.getWorld().isClient;
        if (!this.entity.isGliding())
        {
            // Bounce up
            if (this.entity.age == this.bounceAge || (this.entity.age == this.bounceAge + 1) || (this.entity.age == this.bounceAge - 1))
            {
                Vec3d motion = this.entity.getVelocity();
                this.entity.setVelocity(motion.x, this.bounce, motion.z);
                this.entity.setOnGround(false);
                this.entity.velocityDirty = true;

                Groves.LOGGER.info("BouncingHandler.endTick({}): ages - {}, {}, v - {}", isRemote, this.entity.age, this.bounceAge, this.entity.getVelocity());

                this.bounceAge = 0;
            }

            // Preserve motion
            if (!this.entity.isOnGround() && this.entity.age != this.bounceAge)
            {
                Vec3d motion = this.entity.getVelocity();
                if (this.lastVelX != motion.x || this.lastVelZ != motion.z)
                {
                    double f = 0.91D + 0.025D;
                    this.entity.setVelocity(motion.x / f, motion.y, motion.z / f);
                    this.entity.setOnGround(false);
                    this.lastVelX = this.entity.getVelocity().x;
                    this.lastVelZ = this.entity.getVelocity().z;
                }
            }

            // Timing out
            if(this.wasInAir && this.entity.isOnGround())
            {
                if (this.timer == 0)
                {
                    this.timer = this.entity.age;
                }
                else if ((this.entity.age - this.timer) > 5)
                {
                    HANDLERS.remove(this.entity);
                }
            }
            else
            {
                this.timer = 0;
                this.wasInAir = true;
            }
        }
    }

    public static void onEndTick()
    {
        List<BouncingHandler> handlers = HANDLERS.values().stream().toList();

        handlers.forEach(BouncingHandler::endTick);
    }

    public static void addHandler(LivingEntity entity)
    {
        addHandler(entity, 0.0d);
    }

    public static void addHandler(LivingEntity entity, double bounce)
    {
        BouncingHandler handler = HANDLERS.get(entity);
        if (handler == null)
        {
            HANDLERS.put(entity, new BouncingHandler(entity, bounce));
        }

        else if (bounce != 0)
        {
            handler.bounce = bounce;
            handler.bounceAge = entity.age;
        }
    }
}
