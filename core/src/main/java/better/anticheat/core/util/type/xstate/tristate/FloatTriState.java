package better.anticheat.core.util.type.xstate.tristate;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatListIterator;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class FloatTriState implements TriState<Float> {
    private float oldest;
    private boolean hasOldest;
    private float old;
    private boolean hasOld;
    private float current;

    public FloatTriState(float current) {
        this.current = current;
    }

    @NotNull
    @Override
    public FloatListIterator iterator() {
        var list = new FloatArrayList();
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
    public void addNew(Float neww) {
        this.oldest = this.old;
        this.hasOldest = this.hasOld;
        this.old = this.current;
        this.hasOld = true;
        this.current = neww;
    }

    @Override
    public Float getOldestObject() {
        return hasOldest ? oldest : null;
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
