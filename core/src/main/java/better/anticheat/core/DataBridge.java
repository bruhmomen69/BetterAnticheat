package better.anticheat.core;

import com.github.retrooper.packetevents.protocol.player.User;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;

public interface DataBridge {

    boolean hasPermission(User user, String... permission);

    void logInfo(String message);

    void logWarning(String message);

    void sendCommand(String command);

    @Nullable Closeable registerTickListener(User user, Runnable runnable);
    @Nullable Closeable runTaskLater(User user, Runnable runnable, int delayTicks);
}
