package better.anticheat.core.player.tracker.impl.confirmation;

import better.anticheat.core.util.type.incrementer.IntIncrementer;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@EqualsAndHashCode
@ToString
@Getter
@AllArgsConstructor
public class CombinedConfirmation {
    private CompletableFuture<ConfirmationState> onBegin;
    private CompletableFuture<ConfirmationState> onAfterConfirm;
    /**
     * 0 = Sent,
     * 1 = Begun
     * 2 = Confirmed
     */
    private IntIncrementer state = new IntIncrementer(0);

    public CombinedConfirmation(CompletableFuture<ConfirmationState> onBegin, CompletableFuture<ConfirmationState> onAfterConfirm) {
        this.onBegin = onBegin;
        this.onAfterConfirm = onAfterConfirm;
    }

    /**
     * Run the given runnable once the confirmation has been sent to the player.
     * Note that this does not necessarily mean that the player has confirmed the packet.
     *
     * @param consumer the runnable to be ran.
     */
    public CombinedConfirmation onBegin(final Consumer<ConfirmationState> consumer) {
        this.onBegin = this.onBegin.thenApply((a) -> {
            consumer.accept(a);
            return a;
        });

        return this;
    }

    /**
     * Run the given runnable once the confirmation has been sent to the player.
     * Note that this does not necessarily mean that the player has confirmed the packet.
     *
     * @param consumer the runnable to be ran.
     */
    public CombinedConfirmation onBegin(final Runnable consumer) {
        this.onBegin = this.onBegin.thenApply((a) -> {
            consumer.run();
            return a;
        });

        return this;
    }

    public CombinedConfirmation onAfterConfirm(final Consumer<ConfirmationState> consumer) {
        this.onAfterConfirm = this.onAfterConfirm.thenApply((a) -> {
            consumer.accept(a);
            return a;
        });

        return this;
    }

    public CombinedConfirmation onAfterConfirm(final Runnable consumer) {
        this.onAfterConfirm = this.onAfterConfirm.thenApply((a) -> {
            consumer.run();
            return a;
        });
        return this;
    }
}