package better.anticheat.core.util.type.xstate.bistate;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public interface BiState<A> extends Iterable<A> {
    @NotNull
    @Override
    Iterator<A> iterator();

    void flushOld();

    void addNew(A neww);

    A getOldObject();

    A getCurrentObject();

    int hashCode();

    boolean equals(Object o);
}
