package better.anticheat.sponge;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.sponge.listener.CombatDamageListener;
import better.anticheat.sponge.listener.PlayerJoinListener;
import better.anticheat.sponge.listener.ReloadListener;
import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.bstats.sponge.Metrics;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import revxrsal.commands.sponge.SpongeLamp;

import java.nio.file.Path;

@Plugin("betteranticheat")
public class BetterAnticheatSponge {

    private static final int B_STATS_ID = 26305;

    private BetterAnticheat core;
    private final Game game;
    private final Logger logger;
    private final PluginContainer pluginContainer;
    private final Metrics metrics;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDirectory;

    @Inject
    public BetterAnticheatSponge(Game game, Logger logger, PluginContainer pluginContainer, Metrics.Factory metrics) {
        this.game = game;
        this.logger = logger;
        this.pluginContainer = pluginContainer;
        this.metrics = metrics.make(B_STATS_ID);
    }

    @Listener
    public void onServerLoad(final StartingEngineEvent<Server> event) {
        core = new BetterAnticheat(
                new SpongeDataBridge(this, game, logger),
                configDirectory,
                SpongeLamp.builder(this)
        );
    }

    @Listener
    public void onServerStart(final StartedEngineEvent<Server> event) {
        core.enable();
        Sponge.eventManager().registerListeners(pluginContainer, new ReloadListener(core));
        Sponge.eventManager().registerListeners(pluginContainer, new PlayerJoinListener());
        Sponge.eventManager().registerListeners(pluginContainer, new CombatDamageListener());
    }

    @Listener
    public void onServerStop(final StoppingEngineEvent<Server> event) {
        core.disable();
        metrics.shutdown();
    }
}
