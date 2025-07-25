package better.anticheat.core.check;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.configuration.ConfigSection;
import better.anticheat.core.player.Player;
import better.anticheat.core.player.PlayerManager;
import better.anticheat.core.util.ChatUtil;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is the basis for a check in the anticheat. There are a few core components to understand:
 * 1. To use the constructor with only BetterAnticheat, you must use the @CheckInfo annotation on the class.
 * 2. The "vl" is the amount of violations a player has on this check. This will automatically increase when flagged.
 * 3. You can listen to packets coming from the client through handleReceivePlayPacket.
 * 4. You can listen to packets coming from the server through handleSendPlayPacket.
 * 5. The fail method indicates that a player is cheating.
 * 6. If you override the config handling, you must super.load()!
 */
public abstract class Check implements Cloneable {

    protected BetterAnticheat plugin;
    protected Check reference;
    protected Player player;

    @Getter @Setter
    private String name, category, config;
    @Getter
    private final boolean experimental;

    @Getter @Setter
    private boolean enabled = false;
    private int alertVL = 10, verboseVL = 1;
    private Map<Integer, List<String>> punishments = new HashMap<>();

    @Getter
    private int vl = 0;
    private long lastAlertMS = 0;

    /**
     * Construct the check via info provided in CheckInfo annotation.
     * This is the recommended approach but requires a @CheckInfo annotation on implementations.
     */
    public Check(BetterAnticheat plugin) {
        this.plugin = plugin;
        CheckInfo info = this.getClass().getAnnotation(CheckInfo.class);
        if (info == null) throw new InvalidParameterException("No CheckInfo annotation!");

        // Copy values from annotation.
        name = info.name();
        category = info.category();
        config = info.config();
        experimental = info.experimental();
    }

    /**
     * Construct the check via parameters.
     */
    public Check(BetterAnticheat plugin, String name, String category, String config, boolean experimental) {
        this.plugin = plugin;
        this.name = name;
        this.category = category;
        this.config = config;
        this.experimental = experimental;
    }

    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {}

    public void handleSendPlayPacket(PacketPlaySendEvent event) {}

    /*
     * Check set up.
     */

    public void load() {
        if (reference == null) return;
        enabled = reference.enabled;
        alertVL = reference.alertVL;
        punishments = reference.punishments;
        vl = reference.vl;
    }

    public Check initialCopy(Player player) {
        Check check = clone();
        check.reference = this;
        check.player = player;
        check.load();
        return check;
    }

    @Override
    protected Check clone() {
        try {
            return (Check) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    /*
     * Functions.
     */

    /**
     * Handles failing the check for the player with an empty debug.
     */
    protected void fail() {
        fail(null);
    }

    /**
     * Handles failing the check for the player with a debug.
     */
    protected void fail(Object debug) {
        // Prevent unnecessary vl increases.
        vl = Math.min(10000, vl + 1);
        long currentMS = System.currentTimeMillis();
        var deltaAlertMS = currentMS - lastAlertMS;

        /*
         * 1. Ensure alerts are enabled (alertVL != -1)
         * 2. Ensure vl is high enough to alert (vl >= alertVL)
         * 3. Ensure the anti-spam cooldown has elapsed (elapsed >= alertCooldown)
         */
        if (alertVL != -1 && vl >= Math.min(alertVL, verboseVL) &&
                deltaAlertMS >= BetterAnticheat.getInstance().getAlertCooldown() / BetterAnticheat.getInstance().getVerboseCooldownDivisor()) {
            var message = BetterAnticheat.getInstance().getAlertMessage();
            if (!message.isEmpty()) {
                // Build the basic message body.
                message = message.replaceAll("%vl%", String.valueOf(vl));
                message = message.replaceAll("%type%", name);
                message = message.replaceAll("%username%", player.getUser().getName());
                message = ChatUtil.translateColors(message);
                Component finalMessage = Component.text(message);

                // Add the click command to the message. Kyori Adventure's syntax is horrible.
                String click = BetterAnticheat.getInstance().getClickCommand();
                if (!click.isEmpty())
                    finalMessage = finalMessage.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/" + click.replaceAll("%username%", player.getUser().getName())));

                // Assemble and add the hover message.
                final var hoverBuild = new StringBuilder();
                for (final var string : BetterAnticheat.getInstance().getAlertHover()) {
                    hoverBuild.append(string
                                    .replaceAll("%clientversion%", player.getUser().getClientVersion().getReleaseName())
                                    .replaceAll("%debug%", debug == null ? "NO DEBUG" : debug.toString()))
                            .append("\n");
                }

                // An empty message will be length 2 due to the \n on the end.
                if (hoverBuild.length() > 2)
                    finalMessage = finalMessage.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text(ChatUtil.translateColors(hoverBuild.substring(0, hoverBuild.length() - 1)))));

                if (BetterAnticheat.getInstance().isTestMode()) player.getUser().sendMessage(finalMessage);
                else {
                    // Now we know we are sending to staff, we need to determine if we use a verbose, or an alert.
                    // First, we try a verbose, if alert is too low, or cooldown has not elapsed. Otherwise, alert.
                    if (this.vl >= this.verboseVL && (this.vl < this.alertVL || deltaAlertMS < BetterAnticheat.getInstance().getAlertCooldown())) {
                        BetterAnticheat.getInstance().getPlayerManager().sendVerbose(finalMessage);
                    } else if (this.vl >= this.alertVL) {
                        BetterAnticheat.getInstance().getPlayerManager().sendAlert(finalMessage);
                    }
                }
            }

            this.lastAlertMS = currentMS;
        }

        /*
         * We have two punishment systems: modulo and strict.
         * Modulo assumes the punishment should be run whenever the vl is divisible by the setting amount.
         * Strict assumes the punishment should be run whenever the vl is the setting amount.
         */
        if (BetterAnticheat.getInstance().isPunishmentModulo()) {
            for (int punishVL : punishments.keySet()) {
                if (vl % punishVL != 0) continue;
                runPunishment(punishVL);
            }
        } else runPunishment(vl);
    }

    /**
     * Runs the punishment associated with the given vl for the player that this check corresponds to.
     */
    private void runPunishment(int vl) {
        List<String> punishment = punishments.get(vl);
        if (punishment != null) {
            for (String command : punishment) {
                command = command.replaceAll("%username%", player.getUser().getName());
                BetterAnticheat.getInstance().getDataBridge().sendCommand(command);
            }
        }
    }

    /**
     *
     */
    public boolean load(ConfigSection section) {
        if (section == null) {
            enabled = false;
            return false;
        }

        boolean modified = false;

        // Fetch enabled status.
        if (!section.hasNode("enabled")) {
            section.setObject(Boolean.class, "enabled", true);
            modified = true;
        }
        enabled = section.getObject(Boolean.class, "enabled", true);

        // No use in wasting more time loading.
        if (!enabled) return modified;

        // Fetch alertvl.
        if (!section.hasNode("alert-vl")) {
            section.setObject(Integer.class, "alert-vl", 1);
            modified = true;
        }
        alertVL = section.getObject(Integer.class, "alert-vl", 5);

        if (!section.hasNode("verbose-vl")) {
            section.setObject(Integer.class, "verbose-vl", 1);
            modified = true;
        }
        verboseVL = section.getObject(Integer.class, "verbose-vl", 1);

        if (!section.hasNode("punishments")) {
            List<String> defaultPunishments = new ArrayList<>();
            defaultPunishments.add("1:say %username% would be kicked for " + name + "!");
            defaultPunishments.add("5:say %username% would be banned for " + name + "!");
            section.setList(String.class, "punishments", defaultPunishments);
            modified = true;
        }
        punishments.clear();
        List<String> punishmentList = section.getList(String.class, "punishments");
        for (String punishment : punishmentList) {
            String[] elements = punishment.split(":", 2);
            try {
                int vl = Integer.parseInt(elements[0]);
                if (!punishments.containsKey(vl)) punishments.put(vl, new ArrayList<>());
                punishments.get(vl).add(elements[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return modified;
    }
}