package better.anticheat.core.util.type.incrementer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BooleanIncrementer {
    private boolean value = false;

    /**
     * Toggles the boolean value.
     * @return the previous value.
     */
    public boolean toggle() {
        final boolean old = this.value;
        this.value = !this.value;
        return old;
    }

    public boolean get() {
        return this.value;
    }

    public boolean set(final boolean newValue) {
        return this.value = newValue;
    }
}
