package better.anticheat.core.util.type.xstate.bistate;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatListIterator;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class FloatBiState implements BiState<Float> {
    private float old;
    private boolean hasOld;
    private float current;

    public FloatBiState(float current) {
        this.current = current;
    }

    @NotNull
    @Override
    public FloatListIterator iterator() {
        var list = new FloatArrayList();
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
    public void addNew(Float neww) {
        this.old = this.current;
        this.hasOld = true;
        this.current = neww;
    }

    @Override
    public Float getOldObject() {
        return hasOld ? old : null;
    }

    @Override
    public Float getCurrentObject() {
        return current;
    }
}
