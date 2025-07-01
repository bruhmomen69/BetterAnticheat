package better.anticheat.core.util;

import lombok.experimental.UtilityClass;

import java.util.Arrays;

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

    public double getVariance(final double[] data) {
        int count = 0;

        double sum = 0.0;
        double variance = 0.0;

        final double average;

        for (final double number : data) {
            sum += number;
            ++count;
        }

        average = sum / count;

        for (final double number : data) {
            variance += Math.pow(number - average, 2.0);
        }

        return variance;
    }

    public double getStandardDeviation(final double[] data) {
        final double variance = getVariance(data);

        return Math.sqrt(variance);
    }

    public double getSkewness(double[] data) {
        if (data.length < 1) return Double.NaN;

        double sum = 0;
        int count = 0;

        final double[] numbers = new double[data.length];

        for (final double number : data) {
            sum += number;

            numbers[count] = number;
            ++count;
        }

        Arrays.sort(numbers);

        final double mean = sum / count;
        final double median = (count % 2 != 0) ? numbers[count / 2] : (numbers[(count - 1) / 2] + numbers[count / 2]) / 2;
        final double variance = getVariance(data);

        return 3 * (mean - median) / variance;
    }

    public double getAverage(double[] data) {
        double sum = 0.0;

        for (final double number : data) {
            sum += number;
        }

        return sum / data.length;
    }

    public double getFluctuation(double[] collection) {
        double max = 0;
        double min = Double.MAX_VALUE;
        double sum = 0;

        for (double i : collection) {
            sum += i;
            if (i > max) max = i;
            if (i < min) min = i;
        }

        double average = sum / collection.length;
        double median = max - ((max - min) / 2);
        return (average / 50) / (median / 50);
    }

    public double getOscillation(double[] samples) {
        return highest(samples) - lowest(samples);
    }

    public double lowest(double[] numbers) {
        double lowest = Double.MAX_VALUE;
        int i = 0;

        for (double number : numbers) {
            if (number < lowest) {
                lowest = number;
            }
        }

        return lowest;
    }

    public double highest(double[] numbers) {
        double lowest = 0.0D;
        int i = 0;

        for (double number : numbers) {
            if (number > lowest) {
                lowest = number;
            }
        }

        return lowest;
    }

}
