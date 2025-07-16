package better.anticheat.core.util.type.xstate.manystate;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatListIterator;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@Data
public class FloatManyState implements ManyState<Float> {
    private final float[] states;
    private int size = 0;

    public FloatManyState(int capacity) {
        this.states = new float[capacity];
    }

    @NotNull
    @Override
    public FloatListIterator iterator() {
        return new FloatArrayList(Arrays.copyOf(states, size)).iterator();
    }

    @Override
    public void flushOld() {
        if (size > 0) {
            size--;
        }
    }

    @Override
    public void addNew(Float neww) {
        if (capacity() == 0) return;
        int elementsToCopy = Math.min(size, capacity() - 1);
        System.arraycopy(states, 0, states, 1, elementsToCopy);
        states[0] = neww;
        if (size < capacity()) {
            size++;
        } else {
            flushOld();
            addNew(neww);
        }
    }

    @Override
    public Float get(int index) {
        if (index >= size) {
            return null;
        }
        return states[index];
    }

    @Override
    public Float getCurrent() {
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
