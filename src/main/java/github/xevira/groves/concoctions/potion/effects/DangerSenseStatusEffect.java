package github.xevira.groves.concoctions.potion.effects;

import github.xevira.groves.events.client.DangerSenseHandler;
import github.xevira.groves.network.DangerSensePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import org.apache.logging.log4j.core.jmx.Server;

/** Applies a glowing affect (client side) to all hostile entities within range **/
public class DangerSenseStatusEffect extends StatusEffectBase {
    public static final int HORIZONTAL_RANGE = 6;
    public static final int VERTICAL_RANGE = 3;
    public static final int HORIZONTAL_RANGE_GROWTH = 3;
    public static final int VERTICAL_RANGE_GROWTH = 1;
    public static final Box BOUNDING = new Box(-HORIZONTAL_RANGE, -VERTICAL_RANGE, -HORIZONTAL_RANGE, HORIZONTAL_RANGE, VERTICAL_RANGE, HORIZONTAL_RANGE);

    public DangerSenseStatusEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0xFFFF00, 20, false);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayerEntity player)
        {
            ServerPlayNetworking.send(player, new DangerSensePayload());
        }

        return true;
    }

    public static Box getBoundingBox(LivingEntity entity, int amplifier)
    {
        if( amplifier > 0)
            return BOUNDING.expand(HORIZONTAL_RANGE_GROWTH * amplifier, VERTICAL_RANGE_GROWTH * amplifier, HORIZONTAL_RANGE_GROWTH * amplifier).offset(entity.getPos());

        return BOUNDING.offset(entity.getPos());
    }
}
