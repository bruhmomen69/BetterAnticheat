package better.anticheat.core.util.type.fastlist.ord;

import lombok.Getter;

import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * An ArrayFloatEvictingList but it always inserts new values at the end of the array. Useful for ML use cases.
 */
@Getter
public class OrderedArrayFloatEvictingList {
    private final float[] array;
    private final int maxSize;
    private int count = 0;
    /**
     * Is the array full?
     */
    private boolean full = false;

    public OrderedArrayFloatEvictingList(int size) {
        this.array = new float[size];
        this.maxSize = size;
    }

    public DoubleStream stream() {
        return IntStream.range(0, array.length).mapToDouble(i -> array[i]);
    }

    public boolean contains(final float val) {
        for (float v : array) {
            if (v == val) return true;
        }
        return false;
    }

    /**
     * @return the stream as an iterator
     *
     * @deprecated Use #getArray() instead.
     */
    public PrimitiveIterator.OfDouble iterator() {
        return stream().iterator();
    }

    public void push(float item) {
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
