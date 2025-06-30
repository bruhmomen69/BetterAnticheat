package better.anticheat.core.util.type.xstate.bistate;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class ShortBiState implements BiState<Short> {
    private short old;
    private boolean hasOld;
    private short current;

    public ShortBiState(short current) {
        this.current = current;
    }

    @NotNull
    @Override
    public ShortListIterator iterator() {
        var list = new ShortArrayList();
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
    public void addNew(Short neww) {
        this.old = this.current;
        this.hasOld = true;
        this.current = neww;
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
