package better.anticheat.core.util.type.xstate.quadstate;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanListIterator;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class BooleanQuadState implements QuadState<Boolean> {
    private boolean oldest;
    private boolean hasOldest;
    private boolean older;
    private boolean hasOlder;
    private boolean old;
    private boolean hasOld;
    private boolean current;

    public BooleanQuadState(boolean current) {
        this.current = current;
    }

    @NotNull
    @Override
    public BooleanListIterator iterator() {
        var list = new BooleanArrayList();
        if (hasOldest) {
            list.add(oldest);
        }
        if (hasOlder) {
            list.add(older);
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
        this.oldest = this.older;
        this.hasOldest = this.hasOlder;
        this.older = this.old;
        this.hasOlder = this.hasOld;
        this.old = this.current;
        this.hasOld = true;
        this.current = neww;
    }

    @Override
    public Boolean getOldestObject() {
        return hasOldest ? oldest : null;
    }

    @Override
    public Boolean getOlderObject() {
        return hasOlder ? older : null;
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
