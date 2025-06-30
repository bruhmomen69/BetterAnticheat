package better.anticheat.core.util.type.xstate.manystate;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public interface ManyState<A> extends Iterable<A> {
    @NotNull
    @Override
    Iterator<A> iterator();

    void flushOld();

    void addNew(A neww);

    A get(int index);

    A getCurrent();

    int size();

    int capacity();

    int hashCode();

    boolean equals(Object o);
}
