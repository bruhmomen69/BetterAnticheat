package better.anticheat.core.util.type.fastlist;

import lombok.Getter;

import java.util.Arrays;

@Getter
public class ArrayShortEvictingList {
    private final short[] array;
    private final int maxSize;
    private int count = 0;
    /**
     * Is the array full?
     */
    private boolean full = false;

    public ArrayShortEvictingList(int size) {
        this.array = new short[size];
        this.maxSize = size;
    }

    public boolean contains(final short dub) {
        for (short v : array) {
            if (v == dub) return true;
        }
        return false;
    }

    public void push(short item) {
        if (this.count++ >= this.array.length - 1) {
            this.count = 0;
            full = true;
        }

        this.array[count] = item;
    }

    public void removeFirst(short item) {
        for (int i = 0; i < this.array.length; i++) {
            final short v = this.array[i];

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
