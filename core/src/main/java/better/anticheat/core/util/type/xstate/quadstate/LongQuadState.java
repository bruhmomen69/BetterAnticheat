package better.anticheat.core.util.type.xstate.quadstate;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongListIterator;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class LongQuadState implements QuadState<Long> {
    private long oldest;
    private boolean hasOldest;
    private long older;
    private boolean hasOlder;
    private long old;
    private boolean hasOld;
    private long current;

    public LongQuadState(long current) {
        this.current = current;
    }

    @NotNull
    @Override
    public LongListIterator iterator() {
        var list = new LongArrayList();
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
    public void addNew(Long neww) {
        this.oldest = this.older;
        this.hasOldest = this.hasOlder;
        this.older = this.old;
        this.hasOlder = this.hasOld;
        this.old = this.current;
        this.hasOld = true;
        this.current = neww;
    }

    @Override
    public Long getOldestObject() {
        return hasOldest ? oldest : null;
    }

    @Override
    public Long getOlderObject() {
        return hasOlder ? older : null;
    }

    @Override
    public Long getOldObject() {
        return hasOld ? old : null;
    }

    @Override
    public Long getCurrentObject() {
        return current;
    }
}
