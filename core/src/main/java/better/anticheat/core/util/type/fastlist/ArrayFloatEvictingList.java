package better.anticheat.core.util.type.fastlist;

import lombok.Getter;

import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * A size-limited list implementation for storing float values with eviction behavior when the maximum size is reached.
 * This class uses a circular array to store elements and evicts the oldest element based on insertion order.
 * It is optimized for performance in anticheat applications where memory constraints are important.
 * Note that this implementation does not support all standard list operations and is custom-tailored for specific use cases.
 * The stream method returns a DoubleStream, which involves automatic widening from float to double.
 */
@Getter
public class ArrayFloatEvictingList {
    /**
     * The backing array to store float values. Size is fixed at construction.
     */
    private final float[] array;
    /**
     * The maximum number of elements the list can hold. When exceeded, eviction occurs.
     */
    private final int maxSize;
    /**
     * The current index or count for the next insertion. Used to track position in the circular array.
     */
    private int count = 0;
    /**
     * Flag indicating whether the array has reached its maximum size and is full.
     */
    private boolean full = false;

    /**
     * Constructs a new ArrayFloatEvictingList with the specified maximum size.
     * The underlying array is initialized to the given size, and eviction will occur when adding elements beyond this limit.
     *
     * @param size the maximum number of elements the list can hold
     */
    public ArrayFloatEvictingList(int size) {
        this.array = new float[size];
        this.maxSize = size;
    }

    /**
     * Returns a sequential DoubleStream over the elements in this list. Note that float values are widened to double during streaming.
     * The stream is backed by the array and reflects its current contents.
     *
     * @return a stream of double values (widened from float)
     */
    public DoubleStream stream() {
        return IntStream.range(0, array.length).mapToDouble(i -> array[i]);
    }

    /**
     * Checks if the specified float value is contained in the list.
     * This method iterates through the array to find a matching element.
     *
     * @param dub the float value to search for
     * @return true if the value is found, false otherwise
     */
    public boolean contains(final float dub) {
        for (float v : array) {
            if (v == dub) return true;
        }
        return false;
    }

    /**
     * Returns an iterator over the elements in this list as a primitive double iterator.
     * This method is deprecated; use getArray() or stream() for better alternatives, noting the widening to double.
     *
     * @return an iterator over the double values (widened from float)
     * @deprecated Use #getArray() or #stream() instead for direct access or streaming.
     */
    public PrimitiveIterator.OfDouble iterator() {
        return stream().iterator();
    }

    /**
     * Adds a float value to the list. If the list is full, the oldest element is overwritten due to circular behavior.
     * The count is updated to reflect the current position, and the full flag is set if the maximum size is reached.
     *
     * @param item the float value to add
     */
    public void push(float item) {
        if (this.count++ >= this.array.length - 1) {
            this.count = 0;
            full = true;
        }

        this.array[count] = item;
    }

    /**
     * Removes the first occurrence of the specified float value from the list.
     * If found, it is replaced with a sentinel value (-1.0f), and the full flag is reset if necessary.
     * Note that this implementation may not shift elements efficiently; consider alternatives for large lists.
     *
     * @param item the float value to remove
     */
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

    /**
     * Returns a string representation of the list, showing all elements in the array.
     *
     * @return a string representation of the array
     */
    @Override
    public String toString() {
        return Arrays.toString(array);
    }
}
