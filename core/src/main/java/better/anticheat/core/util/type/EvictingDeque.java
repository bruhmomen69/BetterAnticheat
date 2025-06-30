package better.anticheat.core.util.type;

import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Collection;

/**
 * A bounded, evicting deque implementation that extends ArrayDeque, automatically removing the oldest elements when the maximum size is reached.
 * This class is serializable and designed for efficient, memory-bounded collections in multithreaded environments like the anticheat system.
 *
 * @param <T> the type of elements held in this deque
 */
@Getter
public final class EvictingDeque<T> extends ArrayDeque<T> implements Serializable {

    /**
     * The maximum number of elements this deque can hold before eviction occurs.
     */
    private final int maxSize;

    /**
     * Constructs an empty evicting deque with the specified maximum size.
     *
     * @param maxSize the maximum size of the deque; must be positive
     */
    public EvictingDeque(final int maxSize) {
        super(maxSize);
        this.maxSize = maxSize;
    }

    /**
     * Constructs an evicting deque containing the elements of the specified collection, with the given maximum size.
     * Eviction may occur if the collection size exceeds maxSize.
     *
     * @param c the collection whose elements are to be placed into this deque; must not be null
     * @param maxSize the maximum size of the deque; must be positive
     */
    public EvictingDeque(final Collection<? extends T> c, final int maxSize) {
        super(c);
        this.maxSize = maxSize;
    }

    /**
     * Adds the specified element to the end of this deque. If the deque is at maximum capacity, the first element is removed before adding.
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
     * Checks if the deque has reached its maximum capacity.
     *
     * @return true if the deque size is equal to or greater than the maximum size, false otherwise
     */
    public boolean isFull() {
        return size() >= getMaxSize();
    }
}
