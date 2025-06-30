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

    @Override
    public void registerCommands(CommandExceptionHandler<BukkitCommandActor> exceptionHandler, Consumer<ParameterTypes.Builder<BukkitCommandActor>> parameterBuilder, Object... commands) {
        var lampBuilder = BukkitLamp.builder(this.plugin);

        if (exceptionHandler != null) lampBuilder = lampBuilder.exceptionHandler(exceptionHandler);
        if (parameterBuilder != null) lampBuilder = lampBuilder.parameterTypes(parameterBuilder);

        final var lamp = lampBuilder.build();
        lamp.register(commands);
    }

    @Override
    public String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }
}
