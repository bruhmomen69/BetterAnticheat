package better.anticheat.core.util.type.fastlist.ord;

import lombok.Getter;

import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.stream.LongStream;

/**
 * An ArrayLongEvictingList but it always inserts new values at the end of the array. Useful for ML use cases.
 */
@Getter
public class OrderedArrayLongEvictingList {
    private final long[] array;
    private final int maxSize;
    private int count = 0;
    /**
     * Is the array full?
     */
    private boolean full = false;

    public OrderedArrayLongEvictingList(int size) {
        this.array = new long[size];
        this.maxSize = size;
    }

    public LongStream stream() {
        return Arrays.stream(this.array);
    }

    public boolean contains(final long val) {
        for (long v : array) {
            if (v == val) return true;
        }
        return false;
    }

    /**
     * @return the stream as an iterator
     *
     * @deprecated Use #getArray() instead.
     */
    public PrimitiveIterator.OfLong iterator() {
        return stream().iterator();
    }

    public void push(long item) {
        if (this.count++ >= this.array.length - 1) {
            for (int i = 1; i < this.count; i++) {
                array[i - 1] = array[i];
            }

            this.count = this.array.length - 1;
            full = true;
        }

        this.array[count] = item;
    }

    @Override
    public String toString() {
        return Arrays.toString(array);
    }
}
