package better.anticheat.core.command.impl;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.command.Command;
import better.anticheat.core.command.CommandInfo;
import better.anticheat.core.configuration.ConfigSection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.command.CommandActor;

import java.util.ArrayList;
import java.util.List;

/**
 * This command toggles alerts for players, assuming they have the correct permissions.
 * Without sending this command, players will not get sent any alerts of players cheating.
 */
@CommandInfo(name = "mitigate", parent = BACCommand.class)
public class MitigateCommand extends Command {

    private String[] changeOthersPerms;

    public MitigateCommand(BetterAnticheat plugin) {
        super(plugin);
    }

    @CommandPlaceholder
    public void onCommand(final CommandActor actor, int seconds, @Optional final String targetPlayerName) {
        if (!hasPermission(actor)) return;

        final var player = getPlayerFromActor(actor);
        if (player == null) {
            sendReply(actor, Component.text("You must be a player to run this command.").color(TextColor.color(0xFF0000)));
            return;
        }

        if (targetPlayerName == null) {
            player.getCmlTracker().getMitigationTicks().increment(seconds * 20);
            sendReply(actor, Component.text("Player " + " will be mitigated for " + seconds + " seconds").color(TextColor.color(0x00FF00)));
        } else {
            if (!plugin.getDataBridge().hasPermission(player.getUser(), changeOthersPerms)) {
                sendReply(actor, Component.text("You do not have permission to toggle alerts for other players.").color(TextColor.color(0xFF0000)));
                return;
            }

            final var targetPlayer = BetterAnticheat.getInstance().getPlayerManager().getPlayerByUsername(targetPlayerName);
            if (targetPlayer == null) {
                sendReply(actor, Component.text("Player '" + targetPlayerName + "' not found.").color(TextColor.color(0xFF0000)));
                return;
            }

            targetPlayer.getCmlTracker().getMitigationTicks().increment(seconds * 20);
            sendReply(actor, Component.text("Player " + targetPlayerName + " will be mitigated for " + seconds + " seconds").color(TextColor.color(0x00FF00)));
        }
    }

    @Override
    public boolean load(ConfigSection section) {
        boolean modified = super.load(section);

        if (!section.hasNode("change-others-permissions")) {
            List<String> defaultOthers = new ArrayList<>();
            defaultOthers.add("better.anticheat.alerts.others");
            defaultOthers.add("example.permission.node");
            section.setList(String.class, "change-others-permissions", defaultOthers);
        }
        List<String> changeOthersPermsList = section.getList(String.class, "change-others-permissions");
        changeOthersPerms = new String[changeOthersPermsList.size()];
        for (int i = 0; i < changeOthersPermsList.size(); i++) changeOthersPerms[i] = changeOthersPermsList.get(i);

        return modified;
    }
}
