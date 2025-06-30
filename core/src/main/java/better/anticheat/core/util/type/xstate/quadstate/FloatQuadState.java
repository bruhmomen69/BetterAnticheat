package better.anticheat.core.util.type.xstate.quadstate;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatListIterator;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class FloatQuadState implements QuadState<Float> {
    private float oldest;
    private boolean hasOldest;
    private float older;
    private boolean hasOlder;
    private float old;
    private boolean hasOld;
    private float current;

    public FloatQuadState(float current) {
        this.current = current;
    }

    @NotNull
    @Override
    public FloatListIterator iterator() {
        var list = new FloatArrayList();
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
    public void addNew(Float neww) {
        this.oldest = this.older;
        this.hasOldest = this.hasOlder;
        this.older = this.old;
        this.hasOlder = this.hasOld;
        this.old = this.current;
        this.hasOld = true;
        this.current = neww;
    }

    @Override
    public Float getOldestObject() {
        return hasOldest ? oldest : null;
    }

    @Override
    public Float getOlderObject() {
        return hasOlder ? older : null;
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
