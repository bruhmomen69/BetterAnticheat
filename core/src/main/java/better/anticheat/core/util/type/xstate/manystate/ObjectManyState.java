package better.anticheat.core.util.type.xstate.manystate;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;

@Data
public class ObjectManyState<A> implements ManyState<A> {
    private final A[] states;
    private int size = 0;

    public ObjectManyState() {
        this(40);
    }

    @SuppressWarnings("unchecked")
    public ObjectManyState(int capacity) {
        this.states = (A[]) new Object[capacity];
    }

    @NotNull
    @Override
    public Iterator<A> iterator() {
        return new Iterator<>() {
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < size;
            }

            @Override
            public A next() {
                return states[currentIndex++];
            }
        };
    }

    @Override
    public void flushOld() {
        if (size > 0) {
            states[size - 1] = null;
            size--;
        }
    }

    @Override
    public void addNew(A neww) {
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
    public A get(int index) {
        if (index >= size) {
            return null;
        }
        return states[index];
    }

    @Override
    public A getCurrent() {
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
