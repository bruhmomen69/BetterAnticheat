package better.anticheat.core.util.type.incrementer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortIncrementer {
    private short value = 0;

    public short increment() {
        return this.increment((short) 1);
    }

    public short increment(final short amount) {
        if (this.value > (((int) Short.MAX_VALUE) - amount)) {
            this.value = Short.MIN_VALUE;
        }

        return this.value += amount;
    }

    public short decrement() {
        return this.decrement((short) 1);
    }

    public short decrement(final short amount) {
        if (this.value < (Short.MIN_VALUE + amount)) {
            this.value = Short.MAX_VALUE;
        }

        return this.value -= amount;
    }

    public short get() {
        return this.value;
    }

    public short set(final short newValue) {
        return this.value = newValue;
    }
}
