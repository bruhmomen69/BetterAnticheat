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

    @Override
    public void flushOld() {
        this.hasOldest = false;
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
