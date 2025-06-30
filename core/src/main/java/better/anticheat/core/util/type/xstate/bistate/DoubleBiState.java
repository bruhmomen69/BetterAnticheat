package better.anticheat.core.util.type.xstate.bistate;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class DoubleBiState implements BiState<Double> {
    private double old = 0;
    private boolean hasOld = false;
    private double current;

    public DoubleBiState(@Nullable Double old, double current) {
        if (old != null) {
            this.old = old;
            this.hasOld = true;
        }
        this.current = current;
    }

    public DoubleBiState(double current) {
        this.current = current;
    }

    @NotNull
    @Override
    public DoubleListIterator iterator() {
        var list = new DoubleArrayList();
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
    public void addNew(Double neww) {
        this.old = this.current;
        this.current = neww;
    }

    @Override
    public Double getOldObject() {
        return this.hasOld ? this.old : null;
    }

    @Override
    public Double getCurrentObject() {
        return this.current;
    }

    public void addNew(double neww) {
        this.old = this.current;
        this.current = neww;
    }
}
