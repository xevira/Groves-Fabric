package github.xevira.groves.util;

import net.minecraft.util.math.ColorHelper;

public class ColorPulser {
    private final int start;
    private final int end;
    private float delta;
    private float rate;

    private int color;

    public ColorPulser(final int start, final int end, final float rate)
    {
        this.start = start;
        this.end = end;
        this.rate = rate;
        this.delta = 0.0f;

        this.color = start;
    }

    public void tick()
    {
        this.delta += this.rate;

        if (this.delta >= 1.0f)
        {
            this.delta = 1.0f;
            this.rate = -this.rate;
        }
        else if (this.delta <= 0.0f)
        {
            this.delta = 0.0f;
            this.rate = -this.rate;
        }

        this.color = ColorHelper.lerp(this.delta, this.start, this.end);
    }

    public int getColor()
    {
        return this.color;
    }
}
