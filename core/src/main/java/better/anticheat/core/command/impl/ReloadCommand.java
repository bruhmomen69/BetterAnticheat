package better.anticheat.core.command.impl;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.command.Command;
import better.anticheat.core.command.CommandInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.command.CommandActor;

@CommandInfo(name = "reload", config = "commands", parent = BACCommand.class)
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
