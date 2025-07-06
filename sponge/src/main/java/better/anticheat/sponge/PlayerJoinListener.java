package better.anticheat.sponge;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.player.PlayerManager;
import com.github.retrooper.packetevents.PacketEvents;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

public class PlayerJoinListener {

    @Listener
    public void onPlayerJoin(ServerSideConnectionEvent.Join event) {
        var user = PacketEvents.getAPI().getPlayerManager().getUser(event.player());
        if (user == null) return;

        var player = BetterAnticheat.getInstance().getPlayerManager().getPlayer(user);
        if (player == null) return;

        if (event.player().hasPermission(BetterAnticheat.getInstance().getAlertPermission())) {
            player.setAlerts(true);
        }
    }
}
