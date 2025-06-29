package better.anticheat.core.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;

/**
 * Faster alternative to java.util.stream, often involving zero allocations
 */
@UtilityClass
public class EasyLoops {
    public <A> boolean anyMatch(final Collection<A> collection, final Predicate<A> predicate) {
        for (A predictedMotion : collection) {
            if (predicate.test(predictedMotion)) return true;
        }

        return false;
    }

    public <A> boolean allMatch(final Collection<A> collection, final Predicate<A> predicate) {
        for (A predictedMotion : collection) {
            if (!predicate.test(predictedMotion)) return false;
        }

        return true;
    }

    public <A> @NotNull List<A> filterArrayList(final Collection<A> in, final List<A> out, final Predicate<A> predicate) {
        for (A predictedMotion : in) {
            if (predicate.test(predictedMotion)) out.add(predictedMotion);
        }

        return out;
    }

    public <A> @NotNull Queue<A> filterQueue(final Collection<A> in, final Queue<A> out, final Predicate<A> predicate) {
        for (A predictedMotion : in) {
            if (predicate.test(predictedMotion)) out.add(predictedMotion);
        }

        return out;
    }

    public <A> A findFirst(final Collection<A> collection, final Predicate<A> predicate) {
        for (A item : collection) {
            if (predicate.test(item)) {
                return item;
            }
        }
        return null;
    }
}
