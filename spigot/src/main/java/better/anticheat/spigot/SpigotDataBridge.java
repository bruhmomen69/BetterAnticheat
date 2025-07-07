package better.anticheat.spigot;

import better.anticheat.core.DataBridge;
import com.github.retrooper.packetevents.protocol.player.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.exception.CommandExceptionHandler;
import revxrsal.commands.parameter.ParameterTypes;

import java.io.Closeable;
import java.util.function.Consumer;

public class SpigotDataBridge implements DataBridge<BukkitCommandActor> {

    private final BetterAnticheatSpigot plugin;

    public SpigotDataBridge(BetterAnticheatSpigot plugin) {
        this.plugin = plugin;
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
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
    }

    @Override
    public Closeable registerTickListener(User user, Runnable runnable) {
        final var task = new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }.runTaskTimer(plugin, 0, 1);

        return task::cancel;
    }

    @Override
    public Closeable runTaskLater(User user, Runnable runnable, int delayTicks) {
        final var task = new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }.runTaskLater(plugin, delayTicks);

        return task::cancel;
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
}
