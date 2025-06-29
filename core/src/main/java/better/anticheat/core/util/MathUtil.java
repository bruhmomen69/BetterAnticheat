package better.anticheat.core.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MathUtil {
    public double hypot(final double... number) {
        double sum = 0.0;

        for (final double v : number) {
            sum += v * v;
        }

        return Math.sqrt(sum);
    }
}
