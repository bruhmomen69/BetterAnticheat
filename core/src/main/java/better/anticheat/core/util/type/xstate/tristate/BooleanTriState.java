package better.anticheat.core.util.type.xstate.tristate;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanListIterator;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class BooleanTriState implements TriState<Boolean> {
    private boolean oldest;
    private boolean hasOldest;
    private boolean old;
    private boolean hasOld;
    private boolean current;

    public BooleanTriState(boolean current) {
        this.current = current;
    }

    @NotNull
    @Override
    public BooleanListIterator iterator() {
        var list = new BooleanArrayList();
        if (hasOldest) {
            list.add(oldest);
        }
        if (hasOld) {
            list.add(old);
        }
        list.add(current);
        return list.iterator();
    }

    /**
     * Checks if any of the states (including the current one) are {@code true}.
     *
     * @return {@code true} if any of the states are {@code true}, {@code false} otherwise.
     */
    public boolean anyTrue() {
        if (hasOldest && oldest) {
            return true;
        }
        if (hasOld && old) {
            return true;
        }
        return current;
    }

    /**
     * Checks if any of the states (including the current one) are {@code false}.
     *
     * @return {@code true} if any of the states are {@code false}, {@code false} otherwise.
     */
    public boolean anyFalse() {
        if (hasOldest && !oldest) {
            return true;
        }
        if (hasOld && !old) {
            return true;
        }
        return !current;
    }

    @Override
    public void flushOld() {
        if (this.hasOldest) {
            this.hasOldest = false;
        } else if (this.hasOld) {
            this.hasOld = false;
        }
    }

    @Override
    public void addNew(Boolean neww) {
        this.oldest = this.old;
        this.hasOldest = this.hasOld;
        this.old = this.current;
        this.hasOld = true;
        this.current = neww;
    }

    @Override
    public Boolean getOldestObject() {
        return hasOldest ? oldest : null;
    }

    @Override
    public Boolean getOldObject() {
        return hasOld ? old : null;
    }

    @Override
    public Boolean getCurrentObject() {
        return current;
    }
}
