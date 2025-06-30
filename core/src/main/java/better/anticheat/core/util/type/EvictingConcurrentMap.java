package better.anticheat.core.util.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A thread-safe, evicting map implementation that extends ConcurrentHashMap, maintaining a maximum size by automatically removing the least recently added entries when full.
 * This class is designed for scenarios requiring bounded memory usage in concurrent environments, such as tracking data in an anticheat system.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
@RequiredArgsConstructor
public final class EvictingConcurrentMap<K, V> extends ConcurrentHashMap<K, V> {
    /**
     * The maximum number of entries this map can hold before eviction occurs.
     */
    @Getter
    private final int size;
    private final Deque<K> storedKeys = new ArrayDeque<>();

    /**
     * Inserts the specified value with the specified key into this map.
     * If the map is at maximum capacity, the oldest entry is removed before insertion.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with key, or null if there was no mapping for key
     */
    @Override
    public synchronized V put(final K key, final V value) {
        checkAndRemove();
        storedKeys.addLast(key);
        return super.put(key, value);
    }

    /**
     * Copies all of the mappings from the specified map to this map.
     * Each entry is added individually, triggering eviction as needed if the map exceeds its size limit.
     *
     * @param m mappings to be stored in this map; must not be null
     */
    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     * Also removes the key from the internal tracking deque.
     *
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with key, or null if there was no mapping for key
     */
    @Override
    public synchronized V remove(final Object key) {
        storedKeys.remove(key);
        return super.remove(key);
    }

    /**
     * Removes all of the mappings from this map.
     * The map and internal key tracking deque will be empty after this call returns.
     */
    @Override
    public synchronized void clear() {
        storedKeys.clear();
        super.clear();
    }

    /**
     * If the specified key is not already associated with a value, associates it with the given value.
     * Eviction is checked and performed if necessary before insertion.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or null if there was no mapping for the key
     */
    @Override
    public synchronized V putIfAbsent(final K key, final V value) {
        if (!storedKeys.contains(key) || !get(key).equals(value))
            checkAndRemove();
        return super.putIfAbsent(key, value);
    }

    /**
     * Removes the entry for the specified key only if it is currently mapped to the specified value.
     * Also removes the key from the internal tracking deque if the removal is successful.
     *
     * @param key key with which the specified value is associated
     * @param value value expected to be associated with the specified key
     * @return true if the value was removed, false otherwise
     */
    @Override
    public synchronized boolean remove(final Object key, final Object value) {
        storedKeys.remove(key);
        return super.remove(key, value);
    }

    /**
     * Checks if the map has reached its maximum size and removes the oldest entry if necessary.
     * This method is private and used internally to enforce size limits.
     *
     * @return true if an entry was removed due to size limit, false otherwise
     */
    private boolean checkAndRemove() {
        if (storedKeys.size() >= size) {
            final K key = storedKeys.removeFirst();

            remove(key);
            return true;
        }
        return false;
    }
}
