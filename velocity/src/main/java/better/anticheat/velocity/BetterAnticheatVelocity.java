package better.anticheat.velocity;

import better.anticheat.core.BetterAnticheat;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.github.retrooper.packetevents.PacketEvents;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
    id = "betteranticheat",
    name = "BetterAnticheat",
    version = "1.0.0",
    description = "An auxiliary anticheat designed with stability as its focus.",
    authors = {"am noah"}
)
public class BetterAnticheatVelocity {

    private static final int B_STATS_ID = 26391;

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final Metrics.Factory metricsFactory;

    private BetterAnticheat core;
    private Metrics metrics;

    @Inject
    public BetterAnticheatVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory, Metrics.Factory metricsFactory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialization(final ProxyInitializeEvent event) {
        final var dataBridge = new VelocityDataBridge(this, server, logger);
        this.core = new BetterAnticheat(
            dataBridge,
            this.dataDirectory
        );

        this.core.enable();

        PacketEvents.getAPI().getEventManager().registerListener(new CombatDamageListener(this.core));
        this.server.getEventManager().register(this, new PlayerJoinListener(dataBridge));

        this.metrics = metricsFactory.make(this, B_STATS_ID);
    }

    /**
     * Listens for {@link ProxyReloadEvent} and reloads the entire
     * anticheat configuration and data.
     *
     * @param event the event instance
     */
    @Subscribe
    public void onProxyReload(final ProxyReloadEvent event) {
        this.core.load();
    }

    @Subscribe
    public void onProxyShutdown(final ProxyShutdownEvent event) {
        if (this.core != null) {
            this.core.disable();
        }
        if (this.metrics != null) {
            this.metrics.shutdown();
        }
    }
}
