package better.anticheat.core;

import better.anticheat.core.player.Player;
import better.anticheat.core.player.tracker.impl.confirmation.ConfirmationState;
import com.github.retrooper.packetevents.protocol.player.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.CommandActor;

import java.io.Closeable;
import java.util.function.Consumer;

public interface DataBridge<T extends CommandActor> {

    boolean hasPermission(User user, String... permission);

    void logInfo(String message);

    void logWarning(String message);

    void sendCommand(String command);

    @Nullable Closeable registerTickListener(User user, Runnable runnable);
    @Nullable Closeable runTaskLater(User user, Runnable runnable, int delayTicks);

    /**
     * Checks if the platform supports native confirmation
     * @return true if the platform supports native confirmation
     */
    default boolean pfNativeConfirmationSupported() {
        return false;
    }

    /**
     * Runs a native confirmation, if supported, else returns null
     * @param player the player to send the confirmation to in the future.
     * @param runnable the runnable to be ran once the confirmation has been returned.
     * @return the confirmation state, or null if not supported
     */
    default @Nullable ConfirmationState pfNativeConfirmationRun(@NotNull Player player, @NotNull Consumer<ConfirmationState> runnable) {
        return null;
    }

    String getVersion();
}
