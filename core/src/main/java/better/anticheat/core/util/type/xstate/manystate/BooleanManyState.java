package better.anticheat.core.util.type.xstate.manystate;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanListIterator;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@Data
public class BooleanManyState implements ManyState<Boolean> {
    private final boolean[] states;
    private int size = 0;

    public BooleanManyState(int capacity) {
        this.states = new boolean[capacity];
    }

    @NotNull
    @Override
    public BooleanListIterator iterator() {
        return new BooleanArrayList(Arrays.copyOf(states, size)).iterator();
    }

    @Override
    public void flushOld() {
        if (size > 0) {
            size--;
        }
    }

    @Override
    public void addNew(Boolean neww) {
        if (capacity() == 0) return;
        int elementsToCopy = Math.min(size, capacity() - 1);
        System.arraycopy(states, 0, states, 1, elementsToCopy);
        states[0] = neww;
        if (size < capacity()) {
            size++;
        }
    }

    @Override
    public Boolean get(int index) {
        if (index >= size) {
            return null;
        }
        return states[index];
    }

    @Override
    public Boolean getCurrent() {
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
