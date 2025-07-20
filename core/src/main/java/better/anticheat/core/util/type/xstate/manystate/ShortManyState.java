package better.anticheat.core.util.type.xstate.manystate;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@Data
public class ShortManyState implements ManyState<Short> {
    private final short[] states;
    private int size = 0;

    public ShortManyState() {
        this(80);
    }

    public ShortManyState(int capacity) {
        this.states = new short[capacity];
    }

    @NotNull
    @Override
    public ShortListIterator iterator() {
        return new ShortArrayList(Arrays.copyOf(states, size)).iterator();
    }

    @Override
    public void flushOld() {
        if (size > 0) {
            size--;
        }
    }

    @Override
    public void addNew(Short neww) {
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
    public Short get(int index) {
        if (index >= size) {
            return null;
        }
        return states[index];
    }

    @Override
    public Short getCurrent() {
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
