package better.anticheat.core.util.type;

import lombok.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Simple alternative to CompletableFuture for simple, non-thread-safe workloads.
 * @param <T> the type of the result
 */
@ToString
@EqualsAndHashCode
public class SimpleFuture<T> {
    private final ArrayList<Consumer<T>> listeners = new ArrayList<>();
    private @Nullable T result;

    private boolean complete;

    public SimpleFuture() {
        this.complete = false;
    }

    public SimpleFuture(final @Nullable T result) {
        this.result = result;
        this.complete = true;
    }

    public synchronized SimpleFuture<T> addListener(final Consumer<T> listener) {
        if (complete) {
            listener.accept(this.result);
        } else {
            this.listeners.add(listener);
        }

        return this;
    }

    /**
     * Completes the future if incomplete.
     * @param result the result to complete with
     * @return true if the future was incomplete
     */
    public synchronized boolean completeIfIncomplete(final T result) {
        if (this.complete) {
            return false;
        }

        this.complete = true;
        this.result = result;
        this.listeners.forEach(listener -> listener.accept(result));
        return true;
    }

    public synchronized SimpleFuture<T> complete(final T result) throws IllegalStateException {
        if (!completeIfIncomplete(result)) {
            throw new IllegalStateException("Future already completed");
        }
        return this;
    }

    public synchronized boolean isComplete() {
        return complete;
    }

    public synchronized @Nullable T getResult() {
        return result;
    }
}
