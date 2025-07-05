package better.anticheat.paper;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.player.PlayerManager;
import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        var user = PacketEvents.getAPI().getPlayerManager().getUser(event.getPlayer());
        if (user == null) return;

        var player = PlayerManager.getPlayer(user);
        if (player == null) return;

        if (event.getPlayer().hasPermission(BetterAnticheat.getInstance().getAlertPermission())) {
            player.setAlerts(true);
        }
    }
}
