package github.xevira.groves.block;

import github.xevira.groves.Groves;
import github.xevira.groves.util.LunarPhasesEnum;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ChiseledMoonstoneBricksBlock extends MoonstoneBrickBlock {
    private final LunarPhasesEnum phase;

    public ChiseledMoonstoneBricksBlock(LunarPhasesEnum phase, Settings settings) {
        super(settings);

        this.phase = phase;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType options) {
        tooltip.add(Groves.text("lore", "lunar_phase." + phase.toString()).formatted(Formatting.BLUE));

        super.appendTooltip(stack, context, tooltip, options);
    }
}
