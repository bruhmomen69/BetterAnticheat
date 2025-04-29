package better.anticheat.core;

import better.anticheat.core.check.CheckManager;
import better.anticheat.core.user.PacketListener;
import better.anticheat.core.user.UserManager;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import sharkbyte.configuration.core.ConfigSection;

import java.util.List;

public class BetterAnticheat {

    private static BetterAnticheat instance;

    private final DataBridge dataBridge;

    private boolean enabled;

    // Settings
    private int alertCooldown;
    private List<String> alertHover;
    private String alertMessage, alertPermission, clickCommand;
    private boolean punishmentModulo, testMode;

    public BetterAnticheat(DataBridge dataBridge) {
        this.dataBridge = dataBridge;
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
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListener());
        load();
    }

    public void disable() {
        enabled = false;
    }

    public void load() {
        if (!enabled) return;

        dataBridge.logInfo("Beginning load!");

        ConfigSection settings = dataBridge.getConfigurationFile("settings.yml", BetterAnticheat.class.getResourceAsStream("/settings.yml")).load();
        alertCooldown = settings.getObject(Integer.class, "alert-cooldown", 1000);
        alertPermission = settings.getObject(String.class, "alert-permission", "better.anticheat");
        alertHover = settings.getList(String.class, "alert-hover");
        alertMessage = settings.getObject(String.class, "alert-message", "");
        clickCommand = settings.getObject(String.class, "click-command", "");
        punishmentModulo = settings.getObject(Boolean.class, "punishment-modulo", true);
        testMode = settings.getObject(Boolean.class, "test-mode", false);

        CheckManager.load(this);
        UserManager.load();

        dataBridge.logInfo("Load finished!");
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