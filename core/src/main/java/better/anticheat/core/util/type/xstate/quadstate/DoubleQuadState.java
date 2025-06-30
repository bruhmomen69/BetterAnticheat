package better.anticheat.core.util.type.xstate.quadstate;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class DoubleQuadState implements QuadState<Double> {
    private double oldest;
    private boolean hasOldest;
    private double older;
    private boolean hasOlder;
    private double old;
    private boolean hasOld;
    private double current;

    public DoubleQuadState(double current) {
        this.current = current;
    }

    @NotNull
    @Override
    public DoubleListIterator iterator() {
        var list = new DoubleArrayList();
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
    public void addNew(Double neww) {
        this.oldest = this.older;
        this.hasOldest = this.hasOlder;
        this.older = this.old;
        this.hasOlder = this.hasOld;
        this.old = this.current;
        this.hasOld = true;
        this.current = neww;
    }

    @Override
    public Double getOldestObject() {
        return hasOldest ? oldest : null;
    }

    @Override
    public Double getOlderObject() {
        return hasOlder ? older : null;
    }

    @Override
    public Double getOldObject() {
        return hasOld ? old : null;
    }

    @Override
    public Double getCurrentObject() {
        return current;
    }
}
