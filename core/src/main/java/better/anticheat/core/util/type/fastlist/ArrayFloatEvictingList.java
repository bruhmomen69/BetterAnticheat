package better.anticheat.core.util.type.fastlist;

import lombok.Getter;

import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

@Getter
public class ArrayFloatEvictingList {
    private final float[] array;
    private final int maxSize;
    private int count = 0;
    /**
     * Is the array full?
     */
    private boolean full = false;

    public ArrayFloatEvictingList(int size) {
        this.array = new float[size];
        this.maxSize = size;
    }

    public DoubleStream stream() {
        return IntStream.range(0, array.length).mapToDouble(i -> array[i]);
    }

    public boolean contains(final float dub) {
        for (float v : array) {
            if (v == dub) return true;
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
            this.count = 0;
            full = true;
        }

        this.array[count] = item;
    }

    public void removeFirst(float item) {
        for (int i = 0; i < this.array.length; i++) {
            final float v = this.array[i];

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
