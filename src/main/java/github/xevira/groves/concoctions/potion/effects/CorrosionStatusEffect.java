package github.xevira.groves.concoctions.potion.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;

/** Damages items worn by entity **/
public class CorrosionStatusEffect extends StatusEffectBase {
    private Random random = Random.create();

    public CorrosionStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0x37A302, 40, true);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        List<ItemStack> items = new ArrayList<>();

        for (ItemStack item : entity.getEquippedItems()) {
            if (item.isDamageable())
                items.add(item);
        }

        if (!items.isEmpty()) {
            int index = random.nextInt(items.size());

            ItemStack stack = items.get(index);
            if (!stack.willBreakNextUse())
            {
                stack.damage(1, world, null, itm -> {});
            }
        }

        return true;
    }
}
