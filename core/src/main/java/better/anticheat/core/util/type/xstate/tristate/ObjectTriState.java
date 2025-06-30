package better.anticheat.core.util.type.xstate.tristate;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;

@Data
@NoArgsConstructor
public class ObjectTriState<A> implements TriState<A> {
    private @Nullable A oldest = null;
    private @Nullable A old = null;
    private @NotNull A current;

    public ObjectTriState(@Nullable A oldest, @Nullable A old, @NotNull A current) {
        this.oldest = oldest;
        this.old = old;
        this.current = current;
    }

    public ObjectTriState(@NotNull A current) {
        this.current = current;
    }

    @NotNull
    @Override
    public Iterator<A> iterator() {
        var list = new ArrayList<A>();
        if (oldest != null) {
            list.add(oldest);
        }
        if (old != null) {
            list.add(old);
        }
        list.add(current);
        return list.iterator();
    }

    @Override
    public void flushOld() {
        this.oldest = null;
    }

    @Override
    public void addNew(A neww) {
        this.oldest = this.old;
        this.old = this.current;
        this.current = neww;
    }

    @Override
    public A getOldestObject() {
        return oldest;
    }

    @Override
    public A getOldObject() {
        return old;
    }

    @Override
    public A getCurrentObject() {
        return current;
    }
}
