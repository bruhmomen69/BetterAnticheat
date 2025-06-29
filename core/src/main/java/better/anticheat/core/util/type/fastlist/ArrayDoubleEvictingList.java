package better.anticheat.core.util.type.fastlist;

import lombok.Getter;

import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.stream.DoubleStream;

@Getter
public class ArrayDoubleEvictingList {
    private final double[] array;
    private final int maxSize;
    private int count = 0;
    /**
     * Is the array full?
     */
    private boolean full = false;

    public ArrayDoubleEvictingList(int size) {
        this.array = new double[size];
        this.maxSize = size;
    }

    public DoubleStream stream() {
        return Arrays.stream(this.array);
    }

    public boolean contains(final double dub) {
        for (double v : array) {
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

    public void push(double item) {
        if (this.count++ >= this.array.length - 1) {
            this.count = 0;
            full = true;
        }

        this.array[count] = item;
    }

    public void removeFirst(double item) {
        for (int i = 0; i < this.array.length; i++) {
            final double v = this.array[i];

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
