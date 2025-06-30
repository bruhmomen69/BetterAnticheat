package better.anticheat.core.util.type.fastlist;

import lombok.Getter;

import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.stream.LongStream;

/**
 * A size-limited list implementation for storing long values with eviction behavior when the maximum size is reached.
 * This class uses a circular array to store elements and evicts the oldest element based on insertion order.
 * It is optimized for performance in anticheat applications where memory constraints are important.
 * Note that this implementation does not support all standard list operations and is custom-tailored for specific use cases.
 * The maxSize field is of type long, but the constructor accepts an int; ensure size values are within appropriate limits.
 *
 * @see LongStream for streaming capabilities
 */
@Getter
public class ArrayLongEvictingList {
    /**
     * The backing array to store long values. Size is fixed at construction.
     */
    private final long[] array;
    /**
     * The maximum number of elements the list can hold. Declared as long, but set via an int parameter in the constructor.
     */
    private final long maxSize;
    /**
     * The current index or count for the next insertion. Used to track position in the circular array.
     */
    private int count = 0;
    /**
     * Flag indicating whether the array has reached its maximum size and is full.
     */
    private boolean full = false;

    /**
     * Constructs a new ArrayLongEvictingList with the specified maximum size.
     * The underlying array is initialized to the given size, and eviction will occur when adding elements beyond this limit.
     * Note that maxSize is stored as long, but the input size is int; values should be non-negative.
     *
     * @param size the maximum number of elements the list can hold
     */
    public ArrayLongEvictingList(int size) {
        this.array = new long[size];
        this.maxSize = size;
    }

    /**
     * Returns a sequential LongStream over the elements in this list.
     * The stream is backed by the array and reflects its current contents.
     *
     * @return a stream of long values
     */
    public LongStream stream() {
        return Arrays.stream(this.array);
    }

    /**
     * Checks if the specified long value is contained in the list.
     * This method iterates through the array to find a matching element.
     *
     * @param dub the long value to search for
     * @return true if the value is found, false otherwise
     */
    public boolean contains(final long dub) {
        for (long v : array) {
            if (v == dub) return true;
        }
        return false;
    }

    /**
     * Returns an iterator over the elements in this list as a primitive long iterator.
     * This method is deprecated; use getArray() or stream() for better alternatives.
     *
     * @return an iterator over the long values
     * @deprecated Use #getArray() or #stream() instead for direct access or streaming.
     */
    @Deprecated
    public PrimitiveIterator.OfLong iterator() {
        return stream().iterator();
    }

    /**
     * Adds a long value to the list. If the list is full, the oldest element is overwritten due to circular behavior.
     * The count is updated to reflect the current position, and the full flag is set if the maximum size is reached.
     *
     * @param item the long value to add
     */
    public void push(long item) {
        if (this.count++ >= this.array.length - 1) {
            this.count = 0;
            full = true;
        }

        this.array[count] = item;
    }

    /**
     * Removes the first occurrence of the specified long value from the list.
     * If found, it is replaced with a sentinel value (-1L), and the full flag is reset if necessary.
     * Note that this implementation may not shift elements efficiently; consider alternatives for large lists.
     *
     * @param item the long value to remove
     */
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
