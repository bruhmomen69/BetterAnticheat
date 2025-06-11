package better.anticheat.core;

import com.github.retrooper.packetevents.protocol.player.User;

public interface DataBridge {

    boolean hasPermission(User user, String... permission);

    void logInfo(String message);

    void logWarning(String message);

    void sendCommand(String command);
}
