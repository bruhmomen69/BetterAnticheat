package better.anticheat.core.command.impl;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.command.Command;
import better.anticheat.core.command.CommandInfo;
import better.anticheat.core.configuration.ConfigSection;
import better.anticheat.core.player.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.command.CommandActor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    public void onCommand(final CommandActor actor, int seconds, final Player targetPlayer) {
        if (!hasPermission(actor)) return;

        final var player = getPlayerFromActor(actor);
        final var console = actor.name().equalsIgnoreCase("console");
        if (player == null && !console) {
            sendReply(actor, Component.text("You must be a player to run this command.").color(TextColor.color(0xFF0000)));
            return;
        }

        if (!console && !plugin.getDataBridge().hasPermission(player.getUser(), changeOthersPerms)) {
            sendReply(actor, Component.text("You do not have permission to toggle alerts for other players.").color(TextColor.color(0xFF0000)));
            return;
        }

        if (targetPlayer == null) {
            sendReply(actor, Component.text("TargetPlayer was not found").color(TextColor.color(0xFF0000)));
            return;
        }

        targetPlayer.getMitigationTracker().getMitigationTicks().increment(seconds * 20);
        sendReply(actor, Component.text("Player " + targetPlayer.getUser().getName() + " will be mitigated for " + seconds + " seconds").color(TextColor.color(0x00FF00)));

    }

    @Override
    public boolean load(ConfigSection section) {
        boolean modified = super.load(section);

        if (!section.hasNode("mitigate-players")) {
            List<String> defaultOthers = new ArrayList<>();
            defaultOthers.add("better.anticheat.mitigate");
            defaultOthers.add("example.permission.node");
            section.setList(String.class, "change-others-permissions", defaultOthers);
        }
        List<String> changeOthersPermsList = section.getList(String.class, "mitigate-players");
        changeOthersPerms = new String[changeOthersPermsList.size()];
        for (int i = 0; i < changeOthersPermsList.size(); i++) changeOthersPerms[i] = changeOthersPermsList.get(i);

        return modified;
    }
}
