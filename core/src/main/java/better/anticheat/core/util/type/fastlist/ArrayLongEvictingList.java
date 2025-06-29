package better.anticheat.core.util.type.fastlist;

import lombok.Getter;

import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.stream.LongStream;

@Getter
public class ArrayLongEvictingList {
    private final long[] array;
    private final long maxSize;
    private int count = 0;
    /**
     * Is the array full?
     */
    private boolean full = false;

    public ArrayLongEvictingList(int size) {
        this.array = new long[size];
        this.maxSize = size;
    }

    public LongStream stream() {
        return Arrays.stream(this.array);
    }

    public boolean contains(final long dub) {
        for (long v : array) {
            if (v == dub) return true;
        }
        return false;
    }

    /**
     * @return the stream as an iterator
     *
     * @deprecated Use #getArray() instead.
     */
    @Deprecated
    public PrimitiveIterator.OfLong iterator() {
        return stream().iterator();
    }

    public void push(long item) {
        if (this.count++ >= this.array.length - 1) {
            this.count = 0;
            full = true;
        }

        this.array[count] = item;
    }

    public void removeFirst(long item) {
        for (var i = 0; i < this.array.length; i++) {
            final long v = this.array[i];

            if (v == item && count > 0) {
                this.array[i] = this.array[count];
                this.array[count] = -1;
                full = false;
            }
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(array);
    }
}
