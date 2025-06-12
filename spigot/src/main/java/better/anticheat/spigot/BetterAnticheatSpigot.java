package better.anticheat.spigot;

import better.anticheat.core.BetterAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class BetterAnticheatSpigot extends JavaPlugin {

    private BetterAnticheat core;

    @Override
    public void onEnable() {
        core = new BetterAnticheat(new SpigotDataBridge(this), getDataFolder().toPath());
        core.enable();

        if (getServer().getPluginManager().getPlugin("BetterReload") != null) {
            getServer().getPluginManager().registerEvents(new ReloadListener(core), this);
        }
    }

    @Override
    public void onDisable() {
        core.disable();
        HandlerList.unregisterAll(this);
    }
}
