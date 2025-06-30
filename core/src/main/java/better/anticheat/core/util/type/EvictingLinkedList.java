package better.anticheat.core.util.type;

import lombok.Getter;

import java.util.Collection;
import java.util.LinkedList;

/**
 * A bounded, evicting linked list implementation that extends LinkedList, automatically removing the oldest elements when the maximum size is reached.
 * This class is designed for scenarios requiring efficient, memory-bounded collections with standard list operations and eviction support.
 *
 * @param <T> the type of elements held in this list
 */
@Getter
public final class EvictingLinkedList<T> extends LinkedList<T> {

    /**
     * The maximum number of elements this list can hold before eviction occurs.
     */
    private final int maxSize;

    /**
     * Constructs an empty evicting linked list with the specified maximum size.
     *
     * @param maxSize the maximum size of the list; must be positive
     */
    public EvictingLinkedList(final int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * Constructs an evicting linked list initialized with elements from the specified collection and maximum size.
     * Eviction may occur if the collection size exceeds maxSize during addition.
     *
     * @param c the collection of elements to add initially; must not be null
     * @param maxSize the maximum size of the list; must be positive
     */
    public EvictingLinkedList(final Collection<? extends T> c, final int maxSize) {
        this.maxSize = maxSize;
        this.addAll(c);
    }

    /**
     * Adds the specified element to the end of this list. If the list is at maximum capacity, the first element is removed before adding.
     *
     * @param t the element to add
     * @return true if the element was added successfully
     */
    @Override
    public synchronized boolean add(final T t) {
        if (size() >= getMaxSize()) removeFirst();
        return super.add(t);
    }

    /**
     * Adds all of the elements in the specified collection to this list.
     * Each element is added individually, with eviction checked and performed as needed to maintain the maximum size.
     *
     * @param c the collection containing elements to be added; must not be null
     * @return true if the addition was successful
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
