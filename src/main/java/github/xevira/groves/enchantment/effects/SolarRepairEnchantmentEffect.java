package github.xevira.groves.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.xevira.groves.Groves;
import github.xevira.groves.ServerConfig;
import github.xevira.groves.util.BlockHelper;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public record SolarRepairEnchantmentEffect(EnchantmentLevelBasedValue amount) implements EnchantmentEntityEffect {
    private static Random random = Random.create();

    public static final MapCodec<SolarRepairEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    EnchantmentLevelBasedValue.CODEC.fieldOf("amount").forGetter(SolarRepairEnchantmentEffect::amount)
            ).apply(instance, SolarRepairEnchantmentEffect::new));

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        ItemStack itemStack = context.stack();

        if (world.getTime() % 20 != 0) return;

        if (itemStack.contains(DataComponentTypes.MAX_DAMAGE) && itemStack.contains(DataComponentTypes.DAMAGE)) {
            int damage = itemStack.getDamage();
            if (damage > 0) {
                BlockPos blockPos = BlockHelper.Vec3dtoBlockPos(pos);
                int lvl = Math.max((int)this.amount.getValue(level), 1);
                float chance = ServerConfig.getSolarRepairBaseChance() + (lvl - 1) * ServerConfig.getSolarRepairExtraChance();

                if (world.isSkyVisible(blockPos) && world.isDay() && !world.isRaining() && random.nextFloat() < chance) {

                    itemStack.setDamage(damage - 1);
                }
            }
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> getCodec() {
        return CODEC;
    }
}
