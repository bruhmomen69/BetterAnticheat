package better.anticheat.core.util.type.xstate.manystate;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@Data
public class DoubleManyState implements ManyState<Double> {
    private final double[] states;
    private int size = 0;

    public DoubleManyState(int capacity) {
        this.states = new double[capacity];
    }

    @NotNull
    @Override
    public DoubleListIterator iterator() {
        return new DoubleArrayList(Arrays.copyOf(states, size)).iterator();
    }

    @Override
    public void flushOld() {
        if (size > 0) {
            size--;
        }
    }

    @Override
    public void addNew(Double neww) {
        if (capacity() == 0) return;
        int elementsToCopy = Math.min(size, capacity() - 1);
        System.arraycopy(states, 0, states, 1, elementsToCopy);
        states[0] = neww;
        if (size < capacity()) {
            size++;
        }
    }

    @Override
    public Double get(int index) {
        if (index >= size) {
            return null;
        }
        return states[index];
    }

    @Override
    public Double getCurrent() {
        return get(0);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int capacity() {
        return states.length;
    }
}
