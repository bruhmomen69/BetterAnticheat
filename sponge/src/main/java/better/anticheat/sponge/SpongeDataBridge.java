package better.anticheat.sponge;

import better.anticheat.core.DataBridge;
import com.github.retrooper.packetevents.protocol.player.User;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;

import java.io.Closeable;
import java.util.Optional;

public class SpongeDataBridge implements DataBridge {

    private final Game game;
    private final Logger logger;

    public SpongeDataBridge(Game game, Logger logger) {
        this.game = game;
        this.logger = logger;
    }


    @Override
    public boolean hasPermission(User user, String... permission) {
        if (user.getUUID() == null) return false;
        Optional<ServerPlayer> player = game.server().player(user.getUUID());
        if (!player.isPresent()) return false;
        for (String p : permission) if (!player.get().hasPermission(p)) return false;
        return true;
    }

    @Override
    public void logInfo(String message) {
        logger.info(message);
    }

    @Override
    public void logWarning(String message) {
        logger.warn(message);
    }

    @Override
    public void sendCommand(String command) {
        SystemSubject console = Sponge.systemSubject();
        try {
            game.server().commandManager().process(console, command);
        } catch (CommandException e) {
            String message = "Cannot process command \"" + command + "\"!";
            logger.warn(message);
        }
    }

    @Override
    public @Nullable Closeable registerTickListener(User user, Runnable runnable) {
        Task.Builder builder = Task.builder();
        Task task = builder.execute(runnable).interval(Ticks.of(1)).build();
        ScheduledTask scheduledTask = game.server().scheduler().submit(task);
        return scheduledTask::cancel;
    }

    @Override
    public @Nullable Closeable runTaskLater(User user, Runnable runnable, int delayTicks) {
        Task.Builder builder = Task.builder();
        Task task = builder.execute(runnable).delay(Ticks.of(delayTicks)).build();
        ScheduledTask scheduledTask = game.server().scheduler().submit(task);
        return scheduledTask::cancel;
    }
}
