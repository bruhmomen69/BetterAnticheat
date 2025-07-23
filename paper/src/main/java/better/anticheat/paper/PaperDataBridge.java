package better.anticheat.paper;

import better.anticheat.core.DataBridge;
import com.github.retrooper.packetevents.protocol.player.User;
import com.tcoded.folialib.FoliaLib;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.exception.CommandExceptionHandler;
import revxrsal.commands.parameter.ParameterTypes;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class PaperDataBridge implements DataBridge<BukkitCommandActor> {

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
        var player = Bukkit.getPlayer(user.getUUID());
        if (player != null) {
            final var task = lib.getScheduler().runAtEntityTimer(player, runnable, 1, 1);
            if (task != null) {
                return task::cancel;
            }
        }

        final var future = new CompletableFuture<Object>();
        final var cancelled = new AtomicBoolean(false);
        lib.getScheduler().runAsync(task -> {
            var cycles = 0;
            while (!cancelled.get()) {
                var p = Bukkit.getPlayer(user.getUUID());
                if (p != null) {
                    final var innerTask = lib.getScheduler().runAtEntityTimer(p, runnable, 1, 1);
                    if (innerTask != null) {
                        future.whenComplete((a, b) -> innerTask.cancel());
                        return;
                    }
                }
                try {
                    Thread.sleep(50);
                    // 50 Seconds MAX
                    if (cycles++ > 1000) {
                        return;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        return () -> {
            cancelled.set(true);
            future.complete(new Object());
        };
    }

    @Override
    public Closeable runTaskLater(User user, Runnable runnable, int delayTicks) {
        if (user.getUUID() == null) return null;
        var player = Bukkit.getPlayer(user.getUUID());
        if (player != null) {
            final var task = lib.getScheduler().runAtEntityLater(player, runnable, delayTicks);
            if (task != null) {
                return task::cancel;
            }
        }

        final var future = new CompletableFuture<Object>();
        final var cancelled = new AtomicBoolean(false);
        lib.getScheduler().runAsync(task -> {
            var cycles = 0;
            while (!cancelled.get()) {
                var p = Bukkit.getPlayer(user.getUUID());
                if (p != null) {
                    final var innerTask = lib.getScheduler().runAtEntityLater(p, runnable, delayTicks);
                    if (innerTask != null) {
                        future.whenComplete((a, b) -> innerTask.cancel());
                        return;
                    }
                }
                try {
                    Thread.sleep(50);
                    // 50 Seconds MAX
                    if (cycles++ > 1000) {
                        return;
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        return () -> {
            cancelled.set(true);
            future.complete(new Object());
        };
    }

    @Override
    public String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }
}
