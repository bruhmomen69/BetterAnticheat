package better.anticheat.core.util.type;

import lombok.Getter;

import java.util.Collection;
import java.util.LinkedList;

@Getter
public final class EvictingLinkedList<T> extends LinkedList<T> {

    private final int maxSize;

    public EvictingLinkedList(final int maxSize) {
        this.maxSize = maxSize;
    }

    public EvictingLinkedList(final Collection<? extends T> c, final int maxSize) {
        this.maxSize = maxSize;
        this.addAll(c);
    }

    @Override
    public synchronized boolean add(final T t) {
        if (size() >= getMaxSize()) removeFirst();
        return super.add(t);
    }

    @Override
    public boolean addAll(final Collection<? extends T> c) {
        for (T t : c) {
            this.add(t);
        }
        return true;
    }

    public boolean isFull() {
        return size() >= getMaxSize();
    }
}
