package better.anticheat.core.util.type.xstate.quadstate;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;

@Data
@NoArgsConstructor
public class ObjectQuadState<A> implements QuadState<A> {
    private @Nullable A oldest = null;
    private @Nullable A older = null;
    private @Nullable A old = null;
    private @NotNull A current;

    public ObjectQuadState(@Nullable A oldest, @Nullable A older, @Nullable A old, @NotNull A current) {
        this.oldest = oldest;
        this.older = older;
        this.old = old;
        this.current = current;
    }

    public ObjectQuadState(@NotNull A current) {
        this.current = current;
    }

    @NotNull
    @Override
    public Iterator<A> iterator() {
        var list = new ArrayList<A>();
        if (oldest != null) {
            list.add(oldest);
        }
        if (older != null) {
            list.add(older);
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
        this.oldest = this.older;
        this.older = this.old;
        this.old = this.current;
        this.current = neww;
    }

    @Override
    public A getOldestObject() {
        return oldest;
    }

    @Override
    public A getOlderObject() {
        return older;
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
