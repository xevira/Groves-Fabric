package github.xevira.groves.client.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.xevira.groves.item.MoonPhialItem;
import net.minecraft.client.render.item.property.numeric.NumericProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class MoonPhaseProperty {
    public record Overworld() implements NumericProperty {
        public static final MapCodec<MoonPhaseProperty.Overworld> CODEC = MapCodec.unit(new MoonPhaseProperty.Overworld());

        @Override
        public float getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity holder, int seed) {
            return MoonPhialItem.getModelPredicate(stack, world, holder, seed);
        }

        @Override
        public MapCodec<? extends NumericProperty> getCodec() {
            return CODEC;
        }
    }

    public record Unknown() implements NumericProperty {
        public static final MapCodec<Unknown> CODEC = MapCodec.unit(new Unknown());

        @Override
        public float getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity holder, int seed) {
            return MoonPhialItem.getModelPredicate(stack, world, holder, seed);
        }

        @Override
        public MapCodec<? extends NumericProperty> getCodec() {
            return CODEC;
        }
    }
}
