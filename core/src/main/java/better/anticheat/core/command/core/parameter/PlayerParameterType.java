package better.anticheat.core.command.core.parameter;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.player.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;

public class PlayerParameterType implements ParameterType<CommandActor, Player> {
    @Override
    public Player parse(@NotNull MutableStringStream input, @NotNull ExecutionContext context) {
        var contextActor = BetterAnticheat.getInstance().getPlayerManager().getPlayerByUsername(context.actor().name());

        if (!input.hasRemaining()) {
            return contextActor;
        }

        final var read = input.readString();

        if (!read.isEmpty()) {
            var readActor = BetterAnticheat.getInstance().getPlayerManager().getPlayerByUsername(read);

            if (readActor != null) {
                return readActor;
            }

            input.moveBackward(read.length());
        }

        return contextActor;
    }
}
