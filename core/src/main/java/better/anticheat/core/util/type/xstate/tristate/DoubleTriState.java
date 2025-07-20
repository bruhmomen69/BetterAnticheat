package better.anticheat.core.util.type.xstate.tristate;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class DoubleTriState implements TriState<Double> {
    private double oldest;
    private boolean hasOldest;
    private double old;
    private boolean hasOld;
    private double current;

    public DoubleTriState(double current) {
        this.current = current;
    }

    @NotNull
    @Override
    public DoubleListIterator iterator() {
        var list = new DoubleArrayList();
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
        if (this.hasOldest) {
            this.hasOldest = false;
        } else if (this.hasOld) {
            this.hasOld = false;
        }
    }

    @Override
    public void addNew(Double neww) {
        this.oldest = this.old;
        this.hasOldest = this.hasOld;
        this.old = this.current;
        this.hasOld = true;
        this.current = neww;
    }

    @Override
    public Double getOldestObject() {
        return hasOldest ? oldest : null;
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
