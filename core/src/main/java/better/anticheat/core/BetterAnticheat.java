package better.anticheat.core;

import better.anticheat.core.check.CheckManager;
import better.anticheat.core.command.BetterAnticheatCommand;
import better.anticheat.core.configuration.ConfigSection;
import better.anticheat.core.configuration.ConfigurationFile;
import better.anticheat.core.player.PlayerManager;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public class BetterAnticheat {

    private static BetterAnticheat instance;

    private final DataBridge dataBridge;
    private final Path directory;

    private boolean enabled;

    // Settings
    private int alertCooldown;
    private List<String> alertHover;
    private String alertMessage, alertPermission, clickCommand;
    private boolean punishmentModulo, testMode, useCommand;

    public BetterAnticheat(DataBridge dataBridge, Path directory) {
        this.dataBridge = dataBridge;
        this.directory = directory;
        this.enabled = true;

        instance = this;

        /*
         * We only support 1.21+.
         */
        if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_21)) {
            dataBridge.logWarning("You are running on an unsupported version of Minecraft!");
            dataBridge.logWarning("Please update to 1.21 or above!");
            enabled = false;
        }
    }

    public void enable() {
        if (!enabled) return;
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListener(this.dataBridge));
        load();

        if (useCommand) {
            dataBridge.registerCommands(null, null, new BetterAnticheatCommand(this.dataBridge, this.directory));
        }
    }

    public void disable() {
        enabled = false;
    }

    public void load() {
        if (!enabled) return;

        dataBridge.logInfo("Beginning load!");

        ConfigSection settings = getFile("settings.yml", BetterAnticheat.class.getResourceAsStream("/settings.yml")).load();
        alertCooldown = settings.getObject(Integer.class, "alert-cooldown", 1000);
        alertPermission = settings.getObject(String.class, "alert-permission", "better.anticheat");
        alertHover = settings.getList(String.class, "alert-hover");
        alertMessage = settings.getObject(String.class, "alert-message", "");
        clickCommand = settings.getObject(String.class, "click-command", "");
        punishmentModulo = settings.getObject(Boolean.class, "punishment-modulo", true);
        testMode = settings.getObject(Boolean.class, "test-mode", false);
        useCommand = settings.getObject(Boolean.class, "enable-commands", true); // Default to true for people who have not updated their config.

        CheckManager.load(this);
        PlayerManager.load(this);

        dataBridge.logInfo("Load finished!");
    }

    public ConfigurationFile getFile(String name) {
        return new ConfigurationFile(name, directory);
    }

    public ConfigurationFile getFile(String name, InputStream input) {
        return new ConfigurationFile(name, directory, input);
    }

    /*
     * Getters.
     */

    public static BetterAnticheat getInstance() {
        return instance;
    }

    public int getAlertCooldown() {
        return alertCooldown;
    }

    public List<String> getAlertHover() {
        return alertHover;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public String getAlertPermission() {
        return alertPermission;
    }

    public String getClickCommand() {
        return clickCommand;
    }

    public DataBridge getDataBridge() {
        return dataBridge;
    }

    public boolean isPunishmentModulo() {
        return punishmentModulo;
    }

    public boolean isTestMode() {
        return testMode;
    }
}