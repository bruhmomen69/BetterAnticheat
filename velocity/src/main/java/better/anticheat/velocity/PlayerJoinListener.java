package better.anticheat.velocity;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.player.PlayerManager;
import com.github.retrooper.packetevents.PacketEvents;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;

public class PlayerJoinListener {

    private final VelocityDataBridge dataBridge;

    public PlayerJoinListener(final VelocityDataBridge dataBridge) {
        this.dataBridge = dataBridge;
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        var user = PacketEvents.getAPI().getPlayerManager().getUser(event.getPlayer());
        if (user == null) return;

        var player = PlayerManager.getPlayer(user);
        if (player == null) return;

        if (dataBridge.hasPermission(user, BetterAnticheat.getInstance().getAlertPermission())) {
            player.setAlerts(true);
        }
    }
}
