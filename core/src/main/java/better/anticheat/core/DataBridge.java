package better.anticheat.core;

import com.github.retrooper.packetevents.protocol.player.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.CommandExceptionHandler;
import revxrsal.commands.parameter.ParameterTypes;

import java.io.Closeable;
import java.util.function.Consumer;

public interface DataBridge<T extends CommandActor> {

    boolean hasPermission(User user, String... permission);

    void logInfo(String message);

    void logWarning(String message);

    void sendCommand(String command);

    @Nullable Closeable registerTickListener(User user, Runnable runnable);
    @Nullable Closeable runTaskLater(User user, Runnable runnable, int delayTicks);

    String getVersion();
}
