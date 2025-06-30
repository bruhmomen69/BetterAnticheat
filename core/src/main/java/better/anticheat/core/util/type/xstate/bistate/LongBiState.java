package better.anticheat.core.util.type.xstate.bistate;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongListIterator;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class LongBiState implements BiState<Long> {
    private long old;
    private boolean hasOld;
    private long current;

    public LongBiState(long current) {
        this.current = current;
    }

    @NotNull
    @Override
    public LongListIterator iterator() {
        var list = new LongArrayList();
        if (hasOld) {
            list.add(old);
        }
        list.add(current);
        return list.iterator();
    }

    @Override
    public void flushOld() {
        this.hasOld = false;
    }

    @Override
    public void addNew(Long neww) {
        this.old = this.current;
        this.hasOld = true;
        this.current = neww;
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
