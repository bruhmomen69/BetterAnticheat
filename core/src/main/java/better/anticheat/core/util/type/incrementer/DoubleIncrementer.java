package better.anticheat.core.util.type.incrementer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoubleIncrementer {
    private double value = 0;

    public double increment() {
        return this.value++;
    }

    public double increment(final double amount) {
        return this.value += amount;
    }

    public double decrement() {
        return this.value--;
    }

    public double decrement(final double amount) {
        return this.value -= amount;
    }

    public double get() {
        return this.value;
    }

    public double set(final double newValue) {
        return this.value = newValue;
    }
}
