package better.anticheat.sponge;

import better.anticheat.core.BetterAnticheat;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;

public class ReloadListener {

    private final BetterAnticheat plugin;

    public ReloadListener(BetterAnticheat plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onReload(final RefreshGameEvent event) {
        plugin.load();
    }
}
