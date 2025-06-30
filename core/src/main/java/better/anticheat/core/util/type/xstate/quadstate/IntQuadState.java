package better.anticheat.core.util.type.xstate.quadstate;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class IntQuadState implements QuadState<Integer> {
    private int oldest;
    private boolean hasOldest;
    private int older;
    private boolean hasOlder;
    private int old;
    private boolean hasOld;
    private int current;

    public IntQuadState(int current) {
        this.current = current;
    }

    @NotNull
    @Override
    public IntListIterator iterator() {
        var list = new IntArrayList();
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
    public void addNew(Integer neww) {
        this.oldest = this.older;
        this.hasOldest = this.hasOlder;
        this.older = this.old;
        this.hasOlder = this.hasOld;
        this.old = this.current;
        this.hasOld = true;
        this.current = neww;
    }

    @Override
    public Integer getOldestObject() {
        return hasOldest ? oldest : null;
    }

    @Override
    public Integer getOlderObject() {
        return hasOlder ? older : null;
    }

    @Override
    public Integer getOldObject() {
        return hasOld ? old : null;
    }

    @Override
    public Integer getCurrentObject() {
        return current;
    }
}
