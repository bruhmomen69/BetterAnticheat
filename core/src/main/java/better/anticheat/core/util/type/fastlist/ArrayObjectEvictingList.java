package better.anticheat.core.util.type.fastlist;

import lombok.Getter;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * A size-limited list implementation for storing generic object values with eviction behavior when the maximum size is reached.
 * This class uses a circular array to store elements and evicts the oldest element based on insertion order.
 * It is optimized for performance in anticheat applications where memory constraints are important.
 * Note that this implementation does not support all standard list operations and handles null values in contains and removeFirst methods.
 * The array is initialized with Object type and cast to T; ensure type safety in usage.
 *
 * @param <T> the type of elements stored in the list
 * @see Stream for streaming capabilities
 */
@Getter
public class ArrayObjectEvictingList<T> {
    /**
     * The backing array to store object values. Size is fixed at construction and initialized as Object array.
     */
    private final T[] array;
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
     * Constructs a new ArrayObjectEvictingList with the specified maximum size.
     * The underlying array is initialized to the given size using Object array and cast to T.
     * Eviction will occur when adding elements beyond this limit.
     *
     * @param size the maximum number of elements the list can hold
     */
    public ArrayObjectEvictingList(int size) {
        this.array = (T[]) new Object[size];
        this.maxSize = size;
    }

    /**
     * Returns a sequential Stream over the elements in this list.
     * The stream is backed by the array and reflects its current contents, including null values if present.
     *
     * @return a stream of T values
     */
    public Stream<T> stream() {
        return Arrays.stream(this.array);
    }

    /**
     * Checks if the specified object is contained in the list.
     * This method iterates through the array and uses equals for comparison, skipping null elements or handling null input.
     *
     * @param object the object value to search for; can be null
     * @return true if the object is found, false otherwise
     */
    public boolean contains(final T object) {
        for (T v : array) {
            if (v != null && v.equals(object)) return true;
        }
        return false;
    }

    /**
     * Returns an iterator over the elements in this list.
     * This method is deprecated; use getArray() or stream() for better alternatives.
     *
     * @return an iterator over the T values
     * @deprecated Use #getArray() or #stream() instead for direct access or streaming.
     */
    public Iterator<T> iterator() {
        return stream().iterator();
    }

    /**
     * Adds an object value to the list. If the list is full, the oldest element is overwritten due to circular behavior.
     * The count is updated to reflect the current position, and the full flag is set if the maximum size is reached.
     * The item can be null and will be stored as is.
     *
     * @param item the object value to add; can be null
     */
    public void push(T item) {
        if (this.count++ >= this.array.length - 1) {
            this.count = 0;
            full = true;
        }

        this.array[count] = item;
    }

    /**
     * Removes the first occurrence of the specified object value from the list.
     * If found, it is replaced with null, and the full flag is reset if necessary.
     * Comparison uses equals, and null checks are performed to avoid NPE.
     * Note that this implementation may not shift elements efficiently; consider alternatives for large lists.
     *
     * @param item the object value to remove; can be null
     */
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

    /**
     * Returns a string representation of the list, showing all elements in the array, including nulls.
     *
     * @return a string representation of the array
     */
    @Override
    public String toString() {
        return Arrays.toString(array);
    }
}
