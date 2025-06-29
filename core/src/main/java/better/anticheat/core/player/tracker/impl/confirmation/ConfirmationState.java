package better.anticheat.core.player.tracker.impl.confirmation;

import better.anticheat.core.util.type.fastlist.FastObjectArrayList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * Represents the state of a transaction confirmation.
 * This is used to track confirmations sent to the client, which must be acknowledged.
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ConfirmationState {

    private final long id;
    private final ConfirmationType type;
    private final long timestamp;
    private final boolean needsCancellation;
    private final FastObjectArrayList<Runnable> listeners = new FastObjectArrayList<>();

    private long timestampConfirmed = -1L;

    /**
     * Equals should only compare the id and type (the literal confirmation data)
     *
     * @param o the reference object with which to compare.
     * @return true if this object is the same as the obj argument; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfirmationState that = (ConfirmationState) o;
        return id == that.id && type == that.type;
    }

    /**
     * HashCode should only compare the id and type (the literal confirmation data)
     *
     * @return the object hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }
}
