package better.anticheat.core.command;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.DataBridge;
import better.anticheat.core.player.Player;
import better.anticheat.core.player.PlayerManager;
import com.github.retrooper.packetevents.PacketEvents;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.command.CommandActor;

@RequiredArgsConstructor
@Command({"betteranticheat", "bac", "betterac", "antispam"})
@Slf4j
public class BetterAnticheatCommand {
    private final DataBridge<?> dataBridge;
    // Fallback kyori serializer
    private final LegacyComponentSerializer legacyComponentSerializer = LegacyComponentSerializer.builder().hexColors().extractUrls().build();

    @Subcommand("info")
    public void help(final CommandActor actor) {
        if (!hasPermission(actor)) return;
        sendReply(actor, Component.text("BetterAnticheat v" + dataBridge.getVersion()).color(TextColor.color(0x00FF00)));
    }

    private void sendReply(final CommandActor actor, final ComponentLike message) {
        try {
            final var method = actor.getClass().getMethod("reply", ComponentLike.class);
            method.trySetAccessible();
            method.invoke(actor, message);
        } catch (final Exception e) {
            log.error("Failed to find reply method, is your server up to date?", e);
            actor.reply(legacyComponentSerializer.serialize(message.asComponent()));
        }
    }

    private @Nullable Player getUserFromActor(final CommandActor actor) {
        return PlayerManager.getPlayerByName(actor.name());
    }

    private boolean hasPermission(final CommandActor actor) {
        if (actor.name().equalsIgnoreCase("console")) return true;
        var user = getUserFromActor(actor);
        if (user == null) return false;
        return dataBridge.hasPermission(user.getUser(), BetterAnticheat.getInstance().getAlertPermission());
    }
}
