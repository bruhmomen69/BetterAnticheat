package better.anticheat.spigot.listener;

import better.anticheat.core.BetterAnticheat;
import better.reload.api.ReloadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ReloadListener implements Listener {

    private final BetterAnticheat plugin;

    public ReloadListener(BetterAnticheat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onReload(ReloadEvent event) {
        plugin.load();
    }
}
