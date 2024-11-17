package github.xevira.groves.entity;

import com.google.common.collect.ImmutableMap;
import github.xevira.groves.Registration;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class Trades {
    public static final Int2ObjectMap<net.minecraft.village.TradeOffers.Factory[]> DRUID_TRADES = copyToFastUtilMap(
            ImmutableMap.of(
                    0,  // Default set
                    new net.minecraft.village.TradeOffers.Factory[]{
                            new net.minecraft.village.TradeOffers.SellItemFactory(Registration.IMPRINTING_SIGIL_ITEM, 16, 1, 1, 1)
                    },
                    // Unlocked trade sets
                    1,
                    new net.minecraft.village.TradeOffers.Factory[]{
                            new net.minecraft.village.TradeOffers.SellItemFactory(Registration.MOON_PHIAL_ITEM, 16, 1, 1, 1)
                    }
            )
    );

    private static Int2ObjectMap<net.minecraft.village.TradeOffers.Factory[]> copyToFastUtilMap(ImmutableMap<Integer, net.minecraft.village.TradeOffers.Factory[]> map) {
        return new Int2ObjectOpenHashMap<>(map);
    }

}
