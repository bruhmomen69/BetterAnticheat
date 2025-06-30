package better.anticheat.core.util.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * A size-limited map implementation that extends HashMap and evicts the least recently added entries when the maximum size is exceeded.
 * This class is designed for scenarios where memory usage must be controlled, such as in performance-critical anticheat applications.
 * Eviction occurs based on insertion order, removing the oldest entries first.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @see HashMap
 */
@RequiredArgsConstructor
public final class EvictingMap<K, V> extends HashMap<K, V> {
    /**
     * The maximum number of entries allowed in the map. When exceeded, the oldest entry is evicted.
     */
    @Getter
    private final int size;
    /**
     * A deque to track the order of key insertions for eviction purposes.
     */
    private final Deque<K> storedKeys = new ArrayDeque<>();

    /**
     * Associates the specified value with the specified key in this map. If the map size exceeds the maximum size,
     * the least recently added key is removed before adding the new entry.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with key, or null if there was no mapping for key
     */
    @Override
    public V put(final K key, final V value) {
        checkAndRemove();
        storedKeys.addLast(key);
        return super.put(key, value);
    }

    /**
     * Copies all of the mappings from the specified map to this map. Each key-value pair is added using the put method,
     * which may trigger eviction if the size limit is reached.
     *
     * @param m mappings to be stored in this map
     */
    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    /**
     * Removes the mapping for the specified key from this map if present. Also removes the key from the eviction tracking.
     *
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with key, or null if there was no mapping for key
     */
    @Override
    public V remove(final Object key) {
        storedKeys.remove(key);
        return super.remove(key);
    }

    /**
     * Removes all of the mappings from this map. Also clears the eviction tracking deque.
     */
    @Override
    public void clear() {
        storedKeys.clear();
        super.clear();
    }

    /**
     * If the specified key is not already associated with a value or is associated with a different value, associates it with the given value.
     * Eviction may occur if the size limit is reached and the key is added or updated.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or null if there was no mapping for the key
     */
    @Override
    public V putIfAbsent(final K key, final V value) {
        if (!storedKeys.contains(key) || !get(key).equals(value))
            checkAndRemove();
        return super.putIfAbsent(key, value);
    }

    /**
     * Removes the entry for the specified key only if it is currently mapped to the specified value.
     * Also removes the key from the eviction tracking.
     *
     * @param key key with which the specified value is associated
     * @param value value expected to be associated with the specified key
     * @return true if the value was removed, false otherwise
     */
    @Override
    public boolean remove(final Object key, final Object value) {
        storedKeys.remove(key);
        return super.remove(key, value);
    }

    /**
     * Internal method to check if the map size has reached the limit and remove the oldest entry if necessary.
     *
     * @return true if an entry was evicted, false otherwise
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
