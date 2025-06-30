package better.anticheat.core.util.type.xstate.bistate;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class IntBiState implements BiState<Integer> {
    private int old;
    private boolean hasOld;
    private int current;

    public IntBiState(int current) {
        this.current = current;
    }

    @NotNull
    @Override
    public IntListIterator iterator() {
        var list = new IntArrayList();
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
    public void addNew(Integer neww) {
        this.old = this.current;
        this.hasOld = true;
        this.current = neww;
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
