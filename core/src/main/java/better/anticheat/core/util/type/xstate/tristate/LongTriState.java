package better.anticheat.core.util.type.xstate.tristate;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongListIterator;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class LongTriState implements TriState<Long> {
    private long oldest;
    private boolean hasOldest;
    private long old;
    private boolean hasOld;
    private long current;

    public LongTriState(long current) {
        this.current = current;
    }

    @NotNull
    @Override
    public LongListIterator iterator() {
        var list = new LongArrayList();
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
    public void addNew(Long neww) {
        this.oldest = this.old;
        this.hasOldest = this.hasOld;
        this.old = this.current;
        this.hasOld = true;
        this.current = neww;
    }

    @Override
    public Long getOldestObject() {
        return hasOldest ? oldest : null;
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
