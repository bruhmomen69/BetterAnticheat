package better.anticheat.spigot;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.player.PlayerManager;
import io.github.retrooper.packetevents.util.GeyserUtil;
import org.bstats.bukkit.Metrics;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.bukkit.BukkitLamp;

public class BetterAnticheatSpigot extends JavaPlugin {

    private static final int BSTATS_ID = 26212;

    private Metrics metrics;

    private BetterAnticheat core;

    @Override
    public void onEnable() {
        core = new BetterAnticheat(
                new SpigotDataBridge(this),
                getDataFolder().toPath(),
                BukkitLamp.builder(this).build()
        );
        core.enable();

        metrics = new Metrics(this, BSTATS_ID);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new CombatDamageListener(), this);

        if (getServer().getPluginManager().getPlugin("BetterReload") != null) {
            getServer().getPluginManager().registerEvents(new ReloadListener(core), this);
        }

        core.getPlayerManager().registerQuantifier(user -> !GeyserUtil.isGeyserPlayer(user.getUUID()));
    }

    @Override
    public void onDisable() {
        core.disable();
        HandlerList.unregisterAll(this);
        metrics.shutdown();
    }
}
