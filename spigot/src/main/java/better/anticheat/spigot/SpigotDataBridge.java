package better.anticheat.spigot;

import better.anticheat.core.DataBridge;
import com.github.retrooper.packetevents.protocol.player.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.InputStream;

public class SpigotDataBridge implements DataBridge {

    private final BetterAnticheatSpigot plugin;

    public SpigotDataBridge(BetterAnticheatSpigot plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean hasPermission(User user, String... permission) {
        Player player = Bukkit.getPlayer(user.getUUID());
        if (player == null) return false;
        if (player.isOp()) return true;

        for (String string : permission) {
            if (player.hasPermission(string)) return true;
        }

        return false;
    }

    @Override
    public void logInfo(String message) {
        plugin.getLogger().info(message);
    }

    @Override
    public void logWarning(String message) {
        plugin.getLogger().warning(message);
    }

    @Override
    public void sendCommand(String command) {
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
    }
}
