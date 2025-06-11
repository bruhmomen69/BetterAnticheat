package better.anticheat.spigot;

import better.anticheat.core.BetterAnticheat;
import org.bukkit.plugin.java.JavaPlugin;

public class BetterAnticheatSpigot extends JavaPlugin {

    private BetterAnticheat core;

    @Override
    public void onEnable() {
        core = new BetterAnticheat(new SpigotDataBridge(this), getDataFolder().toPath());
        core.enable();
    }

    @Override
    public void onDisable() {
        core.disable();
    }
}
