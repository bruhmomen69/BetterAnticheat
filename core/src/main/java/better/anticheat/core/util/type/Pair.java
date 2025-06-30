package better.anticheat.core.util.type;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A simple generic pair class for holding two values of potentially different types.
 * This class is useful for grouping two objects together without defining a new dedicated class, promoting code simplicity and reusability.
 *
 * @param <X> the type of the first value in the pair
 * @param <Y> the type of the second value in the pair
 */
@Data
@AllArgsConstructor
public class Pair<X, Y> {
    /**
     * The first value of the pair.
     */
    private X x;
    /**
     * The second value of the pair.
     */
    private Y y;
}
