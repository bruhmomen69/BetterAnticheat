package better.anticheat.core.util.type.xstate.manystate;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongListIterator;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@Data
public class LongManyState implements ManyState<Long> {
    private final long[] states;
    private int size = 0;

    public LongManyState() {
        this(40);
    }

    public LongManyState(int capacity) {
        this.states = new long[capacity];
    }

    @NotNull
    @Override
    public LongListIterator iterator() {
        return new LongArrayList(Arrays.copyOf(states, size)).iterator();
    }

    @Override
    public void flushOld() {
        if (size > 0) {
            size--;
        }
    }

    @Override
    public void addNew(Long neww) {
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
    public Long get(int index) {
        if (index >= size) {
            return null;
        }
        return states[index];
    }

    @Override
    public Long getCurrent() {
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
