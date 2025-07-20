package better.anticheat.core.util.type.xstate.manystate;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@Data
public class IntManyState implements ManyState<Integer> {
    private final int[] states;
    private int size = 0;

    public IntManyState() {
        this(80);
    }

    public IntManyState(int capacity) {
        this.states = new int[capacity];
    }

    @NotNull
    @Override
    public IntListIterator iterator() {
        return new IntArrayList(Arrays.copyOf(states, size)).iterator();
    }

    @Override
    public void flushOld() {
        if (size > 0) {
            size--;
        }
    }

    @Override
    public void addNew(Integer neww) {
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
    public Integer get(int index) {
        if (index >= size) {
            return null;
        }
        return states[index];
    }

    @Override
    public Integer getCurrent() {
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
