package better.anticheat.paper;

import better.anticheat.core.DataBridge;
import com.github.retrooper.packetevents.protocol.player.User;
import com.tcoded.folialib.FoliaLib;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.io.Closeable;

public class PaperDataBridge implements DataBridge {

    private final BetterAnticheatPaper plugin;
    private final FoliaLib lib;

    public PaperDataBridge(BetterAnticheatPaper plugin) {
        this.plugin = plugin;
        lib = new FoliaLib(plugin);
    }

    @Override
    public boolean hasPermission(User user, String... permission) {
        // Users in any stage are checked - only go through if they have a UUID.
        if (user.getUUID() == null) return false;
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
        lib.getScheduler().runNextTick((task) -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
    }

    @Override
    public Closeable registerTickListener(User user, Runnable runnable) {
        if (user.getUUID() == null) return null;
        Player player = Bukkit.getPlayer(user.getUUID());
        if (player == null) return null;

        final var task = lib.getScheduler().runAtEntityTimer(player, runnable, 1, 1);
        return task::cancel;
    }

    @Override
    public Closeable runTaskLater(User user, Runnable runnable, int delayTicks) {
        if (user.getUUID() == null) return null;
        Player player = Bukkit.getPlayer(user.getUUID());
        if (player == null) return null;

        final var task = lib.getScheduler().runAtEntityLater(player, runnable, delayTicks);
        return task::cancel;
    }
}
