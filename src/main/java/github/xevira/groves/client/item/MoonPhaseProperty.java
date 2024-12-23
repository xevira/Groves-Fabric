package github.xevira.groves.client.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.xevira.groves.Groves;
import github.xevira.groves.item.MoonPhialItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.property.numeric.NeedleAngleState;
import net.minecraft.client.render.item.property.numeric.NumericProperty;
import net.minecraft.client.render.item.property.numeric.TimeProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class MoonPhaseProperty implements NumericProperty {
    public static final MapCodec<MoonPhaseProperty> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            Codec.BOOL.optionalFieldOf("unknown", Boolean.FALSE).forGetter(property -> property.unknown)
                    )
                    .apply(instance, MoonPhaseProperty::new)
    );

    private final boolean unknown;

    public MoonPhaseProperty(boolean unknown)
    {
        this.unknown = unknown;
    }

    @Override
    public float getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity holder, int seed) {
        if (this.unknown || world == null) return 0;

        return ((world.getMoonPhase()) * 2 + (world.isDay() ? 1 : 0)) * 0.0625f;
    }

    @Override
    public MapCodec<? extends NumericProperty> getCodec() {
        return CODEC;
    }
}
