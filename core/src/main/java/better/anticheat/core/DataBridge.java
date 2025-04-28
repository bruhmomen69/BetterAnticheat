package better.anticheat.core;

import com.github.retrooper.packetevents.protocol.player.User;
import sharkbyte.configuration.core.ConfigurationFile;

import java.io.InputStream;

public interface DataBridge {

    ConfigurationFile getConfigurationFile(String name, InputStream defaultFile);

    boolean hasPermission(User user, String... permission);

    void logInfo(String message);

    void logWarning(String message);

    void sendCommand(String command);
}
