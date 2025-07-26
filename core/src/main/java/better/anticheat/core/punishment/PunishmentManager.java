package better.anticheat.core.punishment;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.check.Check;
import better.anticheat.core.configuration.ConfigSection;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class PunishmentManager {

    private final BetterAnticheat plugin;
    private final Map<String, PunishmentGroup> punishmentGroups = new ConcurrentHashMap<>();
    private final Map<String, Integer> groupViolations = new ConcurrentHashMap<>();

    public void load() {
        punishmentGroups.clear();
        groupViolations.clear();
        ConfigSection section = plugin.getFile("settings.yml").getRoot().getConfigSection("punishment-groups");
        if (section == null) {
            plugin.getDataBridge().logWarning("Punishment groups section not found in settings.yml!");
            return;
        }

        if (!section.hasNode("default")) {
            plugin.getDataBridge().logWarning("Default punishment group not found in settings.yml! Please add it back.");
        }

        for (ConfigSection groupSection : section.getChildren()) {
            String groupName = groupSection.getKey();
            Map<Integer, List<String>> perGroupPunishments = parsePunishments(groupSection.getList(String.class, "per-group-punishments"), groupName);
            Map<Integer, List<String>> perCheckPunishments = parsePunishments(groupSection.getList(String.class, "per-check-punishments"), groupName);
            punishmentGroups.put(groupName, new PunishmentGroup(groupName, perGroupPunishments, perCheckPunishments));
        }
    }

    private Map<Integer, List<String>> parsePunishments(List<String> punishmentList, String groupName) {
        Map<Integer, List<String>> punishments = new HashMap<>();
        for (String punishment : punishmentList) {
            if (punishment == null) continue;
            String[] elements = punishment.split(":", 2);
            if (elements.length != 2) {
                plugin.getDataBridge().logWarning("Could not parse punishment '" + punishment + "' in group '" + groupName + "'. Invalid format.");
                continue;
            }
            try {
                int vl = Integer.parseInt(elements[0]);
                punishments.computeIfAbsent(vl, k -> new ArrayList<>()).add(elements[1]);
            } catch (NumberFormatException e) {
                plugin.getDataBridge().logWarning("Could not parse punishment '" + punishment + "' in group '" + groupName + "'. Invalid VL number.");
            }
        }
        return punishments;
    }

    public PunishmentGroup getPunishmentGroup(String groupName) {
        return punishmentGroups.get(groupName);
    }

    public void runPunishments(Check check) {
        int vl = check.getVl();
        if (plugin.isPunishmentModulo()) {
            for (PunishmentGroup group : check.getPunishmentGroups()) {
                for (int punishVL : group.getPerGroupPunishments().keySet()) {
                    if (vl % punishVL != 0) continue;
                    runPunishment(check, punishVL, group.getPerGroupPunishments());
                }
                for (int punishVL : group.getPerCheckPunishments().keySet()) {
                    if (vl % punishVL != 0) continue;
                    runPunishment(check, punishVL, group.getPerCheckPunishments());
                }
            }
        } else {
            for (PunishmentGroup group : check.getPunishmentGroups()) {
                if (group.getPerGroupPunishments().containsKey(vl)) {
                    runPunishment(check, vl, group.getPerGroupPunishments());
                }
                if (group.getPerCheckPunishments().containsKey(vl)) {
                    runPunishment(check, vl, group.getPerCheckPunishments());
                }
            }
        }
    }

    public void incrementGroupVl(String groupName) {
        groupViolations.put(groupName, groupViolations.getOrDefault(groupName, 0) + 1);
    }

    public int getGroupVl(String groupName) {
        return groupViolations.getOrDefault(groupName, 0);
    }

    private void runPunishment(Check check, int vl, Map<Integer, List<String>> punishmentMap) {
        List<String> punishment = punishmentMap.get(vl);
        if (punishment != null) {
            for (String command : punishment) {
                command = command.replaceAll("%username%", check.getPlayer().getUser().getName());
                command = command.replaceAll("%type%", check.getName());
                plugin.getDataBridge().sendCommand(command);
            }
        }
    }
}
