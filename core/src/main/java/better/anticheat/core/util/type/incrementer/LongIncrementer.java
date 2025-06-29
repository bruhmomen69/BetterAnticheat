package better.anticheat.core.util.type.incrementer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LongIncrementer {
    private long value = 0;

    public long increment() {
        return this.value++;
    }

    public long increment(final long amount) {
        return this.value += amount;
    }

    public long decrement() {
        return this.value--;
    }

    public long decrement(final long amount) {
        return this.value -= amount;
    }

    public long get() {
        return this.value;
    }

    public long set(final long newValue) {
        return this.value = newValue;
    }
}
