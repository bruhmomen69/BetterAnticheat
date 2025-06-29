package better.anticheat.core.util.type.incrementer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FloatIncrementer {
    private float value = 0;

    public float increment() {
        return this.value++;
    }

    public float increment(final float amount) {
        return this.value += amount;
    }

    public float decrement() {
        return this.value--;
    }

    public float decrement(final float amount) {
        return this.value -= amount;
    }

    public float get() {
        return this.value;
    }

    public float set(final float newValue) {
        return this.value = newValue;
    }
}
