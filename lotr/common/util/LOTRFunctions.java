package lotr.common.util;

import net.minecraft.util.MathHelper;

public class LOTRFunctions {
    public static float triangleWave(float t, float min, float max, float period) {
        return min + (max - min) * (Math.abs(t % period / period - 0.5f) * 2.0f);
    }

    public static float normalisedSin(float t) {
        return (MathHelper.sin(t) + 1.0f) / 2.0f;
    }
}
