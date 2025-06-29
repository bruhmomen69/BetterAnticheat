package better.anticheat.core.util.type;

import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Collection;

@Getter
public final class EvictingDeque<T> extends ArrayDeque<T> implements Serializable {

    private final int maxSize;

    public EvictingDeque(final int maxSize) {
        super(maxSize);
        this.maxSize = maxSize;
    }

    public EvictingDeque(final Collection<? extends T> c, final int maxSize) {
        super(c);
        this.maxSize = maxSize;
    }

    @Override
    public synchronized boolean add(final T t) {
        if (size() >= getMaxSize()) removeFirst();
        return super.add(t);
    }

    public boolean isFull() {
        return size() >= getMaxSize();
    }
}
