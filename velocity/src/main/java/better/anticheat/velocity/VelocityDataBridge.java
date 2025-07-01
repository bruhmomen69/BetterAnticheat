package better.anticheat.velocity;

import better.anticheat.core.DataBridge;
import com.github.retrooper.packetevents.protocol.player.User;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;

public class VelocityDataBridge implements DataBridge {

    @Override
    public boolean hasPermission(User user, String... permission) {
        return false;
    }

    @Override
    public void logInfo(String message) {

    }

    @Override
    public void logWarning(String message) {

    }

    @Override
    public void sendCommand(String command) {

    }

    @Override
    public @Nullable Closeable registerTickListener(User user, Runnable runnable) {
        return null;
    }

    @Override
    public @Nullable Closeable runTaskLater(User user, Runnable runnable, int delayTicks) {
        return null;
    }
}
