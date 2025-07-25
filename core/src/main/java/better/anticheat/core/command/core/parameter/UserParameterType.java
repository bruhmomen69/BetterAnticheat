package better.anticheat.core.command.core.parameter;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.player.Player;
import com.github.retrooper.packetevents.protocol.player.User;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;

public class UserParameterType implements ParameterType<CommandActor, User> {
    @Override
    public User parse(@NotNull MutableStringStream input, @NotNull ExecutionContext context) {
        var contextActor = BetterAnticheat.getInstance().getPlayerManager().getPlayerByUsername(context.actor().name());

        if (!input.hasRemaining() && contextActor != null) {
            return contextActor.getUser();
        } else if (!input.hasRemaining()) {
            return null;
        }

        final var read = input.readString();

        if (!read.isEmpty()) {
            var readActor = BetterAnticheat.getInstance().getPlayerManager().getPlayerByUsername(read);

            if (readActor != null) {
                return readActor.getUser();
            }

            input.moveBackward(read.length());
        }

        return contextActor == null ? null : contextActor.getUser();
    }
}
