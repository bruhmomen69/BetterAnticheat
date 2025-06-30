package better.anticheat.core.util.type.xstate.bistate;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanListIterator;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class BooleanBiState implements BiState<Boolean> {
    private boolean old = false;
    private boolean hasOld = false;
    private boolean current;

    public BooleanBiState(@Nullable Boolean old, boolean current) {
        if (old != null) {
            this.old = old;
            this.hasOld = true;
        }
        this.current = current;
    }

    public BooleanBiState(boolean current) {
        this.current = current;
    }

    @NotNull
    @Override
    public BooleanListIterator iterator() {
        var list = new BooleanArrayList();
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
    public void addNew(Boolean neww) {
        this.old = this.current;
        this.current = neww;
    }

    @Override
    public Boolean getOldObject() {
        return this.hasOld ? this.old : null;
    }

    @Override
    public Boolean getCurrentObject() {
        return this.current;
    }

    public void addNew(boolean neww) {
        this.old = this.current;
        this.current = neww;
    }

    public boolean either() {
        return this.current || (this.hasOld & this.old);
    }

    public boolean neither() {
        return !this.current && (!this.hasOld | !this.old);
    }
}
