package better.anticheat.core.command.impl;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.command.Command;
import better.anticheat.core.command.CommandInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.command.CommandActor;

/**
 * This command exists to act as a hub for the other commands.
 * Things like the alerts command should run as subcommands, being accessible like "/bac alerts".
 * I'm not sure how Lamp handles commands without defined logic, so we have this placeholder message.
 */
@CommandInfo(name = "bac", aliases = {"betteranticheat", "betterac", "antispam"})
public class BACCommand extends Command {

    public BACCommand(BetterAnticheat plugin) {
        super(plugin);
    }

    @CommandPlaceholder
    public void onCommand(CommandActor actor) {
        if (!hasPermission(actor)) return;
        sendReply(actor, Component.text("BetterAnticheat v" + plugin.getDataBridge().getVersion()).color(TextColor.color(0x00FF00)));
    }
}
