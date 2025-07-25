package better.anticheat.spigot;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.util.dependencies.DependencyList;
import better.anticheat.spigot.listener.CombatDamageListener;
import better.anticheat.spigot.listener.PlayerJoinListener;
import better.anticheat.spigot.listener.ReloadListener;
import io.github.retrooper.packetevents.util.GeyserUtil;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import org.bstats.bukkit.Metrics;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.bukkit.BukkitLamp;

public class BetterAnticheatSpigot extends JavaPlugin {

    private static final int BSTATS_ID = 26212;

    private Metrics metrics;
    private BetterAnticheat core;

    @Override
    public void onLoad() {
        // Runtime dependency loading to reduce jar size.
        final var libManager = new BukkitLibraryManager(this, "dependencies");

        for (final var repository : DependencyList.REPOSITORIES) {
            libManager.addRepository(repository[1]);
        }

        for (final var dependency : DependencyList.DEPENDENCIES) {
            final var split = dependency.split(":");
            final var groupId = split[0];
            final var artifactId = split[1];
            final var version = split[2];
            final var lib = Library.builder()
                    .groupId(groupId)
                    .artifactId(artifactId)
                    .version(version)
                    .build();
            libManager.loadLibrary(lib);
        }
    }

    @Override
    public void onEnable() {
        this.core = new BetterAnticheat(
                new SpigotDataBridge(this),
                getDataFolder().toPath(),
                BukkitLamp.builder(this)
        );


        this.core.enable();

        this.metrics = new Metrics(this, BSTATS_ID);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new CombatDamageListener(), this);

        if (getServer().getPluginManager().getPlugin("BetterReload") != null) {
            getServer().getPluginManager().registerEvents(new ReloadListener(core), this);
        }

        this.core.getPlayerManager().registerQuantifier(user -> !GeyserUtil.isGeyserPlayer(user.getUUID()));
    }

    @Override
    public void onDisable() {
        this.core.disable();
        HandlerList.unregisterAll(this);
        this.metrics.shutdown();
    }
}
