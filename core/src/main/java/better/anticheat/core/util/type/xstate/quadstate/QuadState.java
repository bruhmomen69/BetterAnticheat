package better.anticheat.core.util.type.xstate.quadstate;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public interface QuadState<A> extends Iterable<A> {
    @NotNull
    @Override
    Iterator<A> iterator();

    void flushOld();

    void addNew(A neww);

    A getOldestObject();

    A getOlderObject();

    A getOldObject();

    A getCurrentObject();

    int hashCode();

    boolean equals(Object o);
}
