package better.anticheat.paper;

import better.anticheat.core.BetterAnticheat;
import org.bstats.bukkit.Metrics;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class BetterAnticheatPaper  extends JavaPlugin {

    private static final int BSTATS_ID = 26212;

    private Metrics metrics;

    private BetterAnticheat core;

    @Override
    public void onEnable() {
        core = new BetterAnticheat(new PaperDataBridge(this), getDataFolder().toPath());
        core.enable();

        metrics = new Metrics(this, BSTATS_ID);

        if (getServer().getPluginManager().getPlugin("BetterReload") != null) {
            getServer().getPluginManager().registerEvents(new ReloadListener(core), this);
        }
    }

    @Override
    public void onDisable() {
        core.disable();
        HandlerList.unregisterAll(this);
        metrics.shutdown();
    }
}
