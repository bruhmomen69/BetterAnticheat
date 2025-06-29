package better.anticheat.core.util.type.fastlist;

import lombok.Getter;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

@Getter
public class ArrayObjectEvictingList<T> {
    private final T[] array;
    private final int maxSize;
    private int count = 0;
    /**
     * Is the array full?
     */
    private boolean full = false;

    public ArrayObjectEvictingList(int size) {
        this.array = (T[]) new Object[size];
        this.maxSize = size;
    }

    public Stream<T> stream() {
        return Arrays.stream(this.array);
    }

    public boolean contains(final T object) {
        for (T v : array) {
            if (v != null && v.equals(object)) return true;
        }
        return false;
    }

    /**
     * @return the stream as an iterator
     *
     * @deprecated Use #getArray() instead.
     */
    public Iterator<T> iterator() {
        return stream().iterator();
    }

    public void push(T item) {
        if (this.count++ >= this.array.length - 1) {
            this.count = 0;
            full = true;
        }

        this.array[count] = item;
    }

    public void removeFirst(T item) {
        for (int i = 0; i < this.array.length; i++) {
            final T v = this.array[i];

            if (v != null && v.equals(item) && count > 0) {
                this.array[i] = this.array[count];
                this.array[count] = null;
                full = false;
            }
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(array);
    }
}
