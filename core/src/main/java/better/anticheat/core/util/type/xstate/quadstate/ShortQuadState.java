package better.anticheat.core.util.type.xstate.quadstate;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class ShortQuadState implements QuadState<Short> {
    private short oldest;
    private boolean hasOldest;
    private short older;
    private boolean hasOlder;
    private short old;
    private boolean hasOld;
    private short current;

    public ShortQuadState(short current) {
        this.current = current;
    }

    @NotNull
    @Override
    public ShortListIterator iterator() {
        var list = new ShortArrayList();
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
    public void addNew(Short neww) {
        this.oldest = this.older;
        this.hasOldest = this.hasOlder;
        this.older = this.old;
        this.hasOlder = this.hasOld;
        this.old = this.current;
        this.hasOld = true;
        this.current = neww;
    }

    @Override
    public Short getOldestObject() {
        return hasOldest ? oldest : null;
    }

    @Override
    public Short getOlderObject() {
        return hasOlder ? older : null;
    }

    @Override
    public Short getOldObject() {
        return hasOld ? old : null;
    }

    @Override
    public Short getCurrentObject() {
        return current;
    }
}
