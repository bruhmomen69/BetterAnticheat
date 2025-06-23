package better.anticheat.core.check;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.configuration.ConfigSection;
import better.anticheat.core.player.Player;
import better.anticheat.core.player.PlayerManager;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Check implements Cloneable {

    private final static Pattern TRANSLATE = Pattern.compile("(?i)&[0-9A-FK-ORX]");

    protected Player player;

    private String type;

    private boolean enabled = false;
    private int alertVL = 0;
    private Map<Integer, List<String>> punishments = new HashMap<>();

    private int vl = 0;
    private long lastAlertMS = 0;

    public Check(String type) {
        this.type = type;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getType() {
        return type;
    }

    public int getVL() {
        return vl;
    }

    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {}

    public void handleSendPlayPacket(PacketPlaySendEvent event) {}

    /*
     * Clone
     */

    public Check copy(Player player) {
        Check check = clone();
        check.player = player;
        check.type = this.type;
        check.enabled = this.enabled;
        check.alertVL = this.alertVL;
        check.punishments = this.punishments;
        check.vl = this.vl;
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
        vl = Math.min(10000, vl + 1);
        long currentMS = System.currentTimeMillis();

        if (alertVL != -1 && vl >= alertVL && (currentMS - lastAlertMS) > BetterAnticheat.getInstance().getAlertCooldown()) {
            String message = BetterAnticheat.getInstance().getAlertMessage();
            if (!message.isEmpty()) {
                message = message.replaceAll("%vl%", String.valueOf(vl));
                message = message.replaceAll("%type%", type);
                message = message.replaceAll("%username%", player.getUser().getName());
                message = translateColors(message);
                Component finalMessage = Component.text(message);

                // Add the click command.
                String click = BetterAnticheat.getInstance().getClickCommand();
                if (!click.isEmpty())
                    finalMessage = finalMessage.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/" + click.replaceAll("%username%", player.getUser().getName())));

                // Assemble and add the hover message.
                StringBuilder hoverBuild = new StringBuilder();
                for (String string : BetterAnticheat.getInstance().getAlertHover()) {
                    hoverBuild.append(string
                                    .replaceAll("%clientversion%", player.getUser().getClientVersion().getReleaseName())
                                    .replaceAll("%debug%", debug == null ? "NO DEBUG" : debug.toString()))
                            .append("\n");
                }
                if (hoverBuild.length() > 2)
                    finalMessage = finalMessage.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text(translateColors(hoverBuild.substring(0, hoverBuild.length() - 1)))));

                if (BetterAnticheat.getInstance().isTestMode()) player.getUser().sendMessage(finalMessage);
                else PlayerManager.sendAlert(finalMessage);
            }

            lastAlertMS = currentMS;
        }

        if (BetterAnticheat.getInstance().isPunishmentModulo()) {
            for (int punishVL : punishments.keySet()) {
                if (vl % punishVL != 0) continue;
                runPunishment(punishVL);
            }
        } else runPunishment(vl);
    }

    private void runPunishment(int vl) {
        List<String> punishment = punishments.get(vl);
        if (punishment != null) {
            for (String command : punishment) {
                command = command.replaceAll("%username%", player.getUser().getName());
                BetterAnticheat.getInstance().getDataBridge().sendCommand(command);
            }
        }
    }

    private static String translateColors(String text) {
        if (text == null) return null;

        Matcher matcher = TRANSLATE.matcher(text);
        StringBuffer output = new StringBuffer();

        while (matcher.find()) {
            String match = matcher.group();
            String replacement = "§" + match.substring(1);
            matcher.appendReplacement(output, replacement);
        }

        matcher.appendTail(output);
        return output.toString();
    }

    public void load(ConfigSection section) {
        if (section == null) {
            enabled = false;
            return;
        }

        enabled = section.getObject(Boolean.class, "enabled", false);
        if (!enabled) return; // No use in wasting more time.
        alertVL = section.getObject(Integer.class, "alert-vl", 1);

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
    }
}