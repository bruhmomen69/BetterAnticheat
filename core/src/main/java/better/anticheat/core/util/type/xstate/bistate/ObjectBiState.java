package better.anticheat.core.util.type.xstate.bistate;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;

@Data
@NoArgsConstructor
public class ObjectBiState<A> implements BiState<A> {
    private @Nullable A old = null;
    private @NotNull A current;

    public ObjectBiState(@Nullable A old, @NotNull A current) {
        this.old = old;
        this.current = current;
    }

    public ObjectBiState(@NotNull A current) {
        this.current = current;
    }

    @NotNull
    @Override
    public Iterator<A> iterator() {
        var list = new ArrayList<A>();
        if (old != null) {
            list.add(old);
        }
        list.add(current);
        return list.iterator();
    }

    @Override
    public void flushOld() {
        this.old = null;
    }

    @Override
    public void addNew(A neww) {
        if (neww == null) throw new NullPointerException("New value was null");

        this.old = this.current;
        this.current = neww;
    }

    @Override
    public A getOldObject() {
        return this.old;
    }

    @Override
    public A getCurrentObject() {
        return this.current;
    }
}
