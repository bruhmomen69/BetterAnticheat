package better.anticheat.core.util.type.incrementer;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class IntIncrementer {
    private int value = 0;

    public int increment() {
        // "safe" overflowing.
        if (this.value > Integer.MAX_VALUE - 1) {
            this.value = Integer.MIN_VALUE;
        }

        return this.value++;
    }

    public int increment(final int amount) {
        // "safe" overflowing.
        if (this.value > Integer.MAX_VALUE - amount) {
            this.value = Integer.MIN_VALUE + (amount - Math.abs(Integer.MAX_VALUE - (Integer.MAX_VALUE - amount)));
        }

        return this.value += amount;
    }

    public int decrement() {
        return this.value--;
    }

    public int decrementOrMin(final int min) {
        return decrementOrMin(1, min);
    }

    public int decrement(final int amount) {
        return this.value -= amount;
    }

    public int decrementOrMin(final int amount, final int min) {
        return this.value = Math.max(this.value - amount, min);
    }

    public int get() {
        return this.value;
    }

    public int set(final int newValue) {
        return this.value = newValue;
    }
}
