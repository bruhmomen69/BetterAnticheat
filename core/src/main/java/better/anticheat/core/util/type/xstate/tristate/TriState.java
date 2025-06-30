package better.anticheat.core.util.type.xstate.tristate;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public interface TriState<A> extends Iterable<A> {
    @NotNull
    @Override
    Iterator<A> iterator();

    void flushOld();

    void addNew(A neww);

    A getOldestObject();

    A getOldObject();

    A getCurrentObject();

    int hashCode();

    boolean equals(Object o);
}
