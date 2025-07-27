package better.anticheat.core.command.core.suggestion;

import better.anticheat.core.BetterAnticheat;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.node.ExecutionContext;
import wtf.spare.sparej.fastlist.FastObjectArrayList;

import java.util.Collection;

public class PlayerNameSuggestionProvider implements SuggestionProvider<CommandActor> {
    @Override
    public @NotNull Collection<String> getSuggestions(@NotNull ExecutionContext<CommandActor> context) {
        final var list = new FastObjectArrayList<String>();

        for (var player : BetterAnticheat.getInstance().getPlayerManager().getUserMap().keySet()) {
            list.add(player.getName());
        }

        return list;
    }
}
