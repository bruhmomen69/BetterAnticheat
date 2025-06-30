package better.anticheat.core.util.type;

import lombok.Getter;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * A thread-safe, evicting list implementation that extends ConcurrentLinkedDeque, maintaining a maximum size by automatically removing the oldest elements when full.
 * This class is designed for use in high-performance scenarios where bounded memory usage is critical, such as in anticheat tracking.
 *
 * @param <T> the type of elements held in this collection
 */
@Getter
public final class ConcurrentEvictingList<T> extends ConcurrentLinkedDeque<T> {

    /**
     * The maximum number of elements this list can hold before eviction occurs.
     */
    private final int maxSize;

    /**
     * Constructs an empty evicting list with the specified maximum size.
     *
     * @param maxSize the maximum size of the list; must be positive
     */
    public ConcurrentEvictingList(final int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * Constructs an evicting list initialized with elements from the given collection and the specified maximum size.
     * Automatically adds all elements from the collection, evicting if necessary to maintain size.
     *
     * @param c the collection of elements to add initially; must not be null
     * @param maxSize the maximum size of the list; must be positive
     */
    public ConcurrentEvictingList(final Collection<? extends T> c, final int maxSize) {
        super();
        this.maxSize = maxSize;

        this.addAll(c);
    }

    /**
     * Adds an element to the end of the list. If the list is at maximum capacity, the oldest element is removed before adding the new one.
     *
     * @param t the element to add
     * @return true if the element was added successfully
     */
    @Override
    public boolean add(final T t) {
        if (size() >= getMaxSize()) removeFirst();
        return super.add(t);
    }

    /**
     * Adds all elements from the specified collection to the list, evicting elements if necessary to maintain the maximum size.
     * Each element is added individually, triggering eviction as needed.
     *
     * @param c the collection containing elements to be added; must not be null
     * @return true, as specified by the Collection interface
     */
    @Override
    public boolean addAll(final Collection<? extends T> c) {
        for (T t : c) {
            this.add(t);
        }
        return true;
    }

    /**
     * Checks if the list has reached its maximum capacity.
     *
     * @return true if the list size is equal to or greater than the maximum size, false otherwise
     */
    public boolean isFull() {
        return size() >= getMaxSize();
    }
}
