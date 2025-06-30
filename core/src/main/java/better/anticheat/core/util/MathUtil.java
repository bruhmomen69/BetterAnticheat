package better.anticheat.core.util;

import lombok.experimental.UtilityClass;

/**
 * Utility class providing mathematical helper methods for the anticheat system.
 */
@UtilityClass
public class MathUtil {
    /**
     * Calculates the Euclidean norm (hypotenuse) of the given double values.
     * This method computes the square root of the sum of squares of the input numbers.
     *
     * @param number the varargs array of double values to compute the norm for
     * @return the Euclidean norm as a double value
     */
    public double hypot(final double... number) {
        double sum = 0.0;

        for (final double v : number) {
            sum += v * v;
        }

        return Math.sqrt(sum);
    }
}
