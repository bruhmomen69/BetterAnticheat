package better.anticheat.velocity.listener;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.velocity.BetterAnticheatVelocity;
import better.anticheat.velocity.VelocityDataBridge;
import com.github.retrooper.packetevents.PacketEvents;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.concurrent.TimeUnit;

public class PlayerJoinListener {

    private final VelocityDataBridge dataBridge;
    private final ProxyServer server;
    private final BetterAnticheatVelocity plugin;

    public PlayerJoinListener(VelocityDataBridge dataBridge, ProxyServer server, BetterAnticheatVelocity plugin) {
        this.dataBridge = dataBridge;
        this.server = server;
        this.plugin = plugin;
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        var user = PacketEvents.getAPI().getPlayerManager().getUser(event.getPlayer());
        if (user == null) return;

        var player = BetterAnticheat.getInstance().getPlayerManager().getPlayer(user);
        if (player == null) {
            // Player join has not been processed yet, wait a second as this is just for alerts.
            server.getScheduler()
                    .buildTask(plugin, () -> {
                        var player1 = BetterAnticheat.getInstance().getPlayerManager().getPlayer(user);

                        if (dataBridge.hasPermission(user, BetterAnticheat.getInstance().getAlertPermission())) {
                            player1.setAlerts(true);
                        }
                    })
                    .delay(1, TimeUnit.SECONDS);
            return;
        }

        if (dataBridge.hasPermission(user, BetterAnticheat.getInstance().getAlertPermission())) {
            player.setAlerts(true);
        }
    }
}
