package better.anticheat.velocity;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.velocity.listener.CombatDamageListener;
import better.anticheat.velocity.listener.PlayerJoinListener;
import better.anticheat.velocity.quantifier.FloodgateQuantifier;
import com.github.retrooper.packetevents.PacketEvents;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;
import revxrsal.commands.Lamp;
import revxrsal.commands.velocity.VelocityLamp;
import revxrsal.commands.velocity.actor.VelocityCommandActor;

import java.nio.file.Path;

import static revxrsal.commands.velocity.VelocityVisitors.brigadier;

@Plugin(
        id = "betteranticheat",
        name = "BetterAnticheat",
        version = "1.0.0",
        description = "An auxiliary anticheat designed with stability as its focus.",
        dependencies = {
                @Dependency(
                        id = "packetevents"
                ),
                @Dependency(
                        id = "floodgate",
                        optional = true
                )
        },
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
        final var lamp = VelocityLamp.builder(this, server).build();
        lamp.accept(brigadier(server));
        this.core = new BetterAnticheat(
                dataBridge,
                this.dataDirectory,
                lamp
        );

        this.core.enable();

        PacketEvents.getAPI().getEventManager().registerListener(new CombatDamageListener(this.core));
        this.server.getEventManager().register(this, new PlayerJoinListener(dataBridge));

        this.metrics = metricsFactory.make(this, B_STATS_ID);

        // PacketEvent GeyserUtil does not exist on the velocity platform.
        if (this.server.getPluginManager().getPlugin("floodgate").isPresent()) {
            this.core.getPlayerManager().registerQuantifier(new FloodgateQuantifier());
        }
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
