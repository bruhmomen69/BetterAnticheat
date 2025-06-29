package better.anticheat.core.util.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public final class EvictingConcurrentMap<K, V> extends ConcurrentHashMap<K, V> {
    @Getter
    private final int size;
    private final Deque<K> storedKeys = new ArrayDeque<>();

    @Override
    public synchronized V put(final K key, final V value) {
        checkAndRemove();
        storedKeys.addLast(key);
        return super.put(key, value);
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public synchronized V remove(final Object key) {
        storedKeys.remove(key);
        return super.remove(key);
    }

    @Override
    public synchronized void clear() {
        storedKeys.clear();
        super.clear();
    }

    @Override
    public synchronized V putIfAbsent(final K key, final V value) {
        if (!storedKeys.contains(key) || !get(key).equals(value))
            checkAndRemove();
        return super.putIfAbsent(key, value);
    }

    @Override
    public synchronized boolean remove(final Object key, final Object value) {
        storedKeys.remove(key);
        return super.remove(key, value);
    }

    private boolean checkAndRemove() {
        if (storedKeys.size() >= size) {
            final K key = storedKeys.removeFirst();

            remove(key);
            return true;
        }
        return false;
    }
}
