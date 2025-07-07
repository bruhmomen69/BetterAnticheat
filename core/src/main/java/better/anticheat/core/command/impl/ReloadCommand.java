package better.anticheat.core.command.impl;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.command.Command;
import better.anticheat.core.command.CommandInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.command.CommandActor;

/**
 * I guess I can't keep running from the inevitable and should include this command! This reloads BetterAnticheat's
 * configuration files when run.
 * I've excluded reload commands in many of my projects for a while to add extra conversion to my BetterReload project,
 * but I don't see the point of doing that when there is genuinely no reason not to implement this here. In
 * BetterScoreboard and BetterLinks I had the cop out of not having a command system, but one exists here already
 * anyway. Still download BetterReload please :).
 */
@CommandInfo(name = "reload", parent = BACCommand.class)
public class ReloadCommand extends Command {

    public ReloadCommand(BetterAnticheat plugin) {
        super(plugin);
    }

    @CommandPlaceholder
    public void onCommand(CommandActor actor) {
        if (!hasPermission(actor)) return;
        sendReply(actor, Component.text("Reloading BetterAnticheat!").color(TextColor.color(0x00FF00)));
        plugin.load();
    }
}
