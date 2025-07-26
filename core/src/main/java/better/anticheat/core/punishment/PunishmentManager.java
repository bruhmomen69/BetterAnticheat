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

    public void load() {
        punishmentGroups.clear();
        ConfigSection section = plugin.getFile("settings.yml").getRoot().getConfigSection("punishment-groups");
        if (section == null) {
            return;
        }

        for (ConfigSection groupSection : section.getChildren()) {
            String groupName = groupSection.getKey();
            Map<Integer, List<String>> perGroupPunishments = new HashMap<>();
            List<String> perGroupPunishmentList = groupSection.getList(String.class, "per-group-punishments");
            for (String punishment : perGroupPunishmentList) {
                String[] elements = punishment.split(":", 2);
                try {
                    int vl = Integer.parseInt(elements[0]);
                    if (!perGroupPunishments.containsKey(vl)) {
                        perGroupPunishments.put(vl, new ArrayList<>());
                    }
                    perGroupPunishments.get(vl).add(elements[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Map<Integer, List<String>> perCheckPunishments = new HashMap<>();
            List<String> perCheckPunishmentList = groupSection.getList(String.class, "per-check-punishments");
            for (String punishment : perCheckPunishmentList) {
                String[] elements = punishment.split(":", 2);
                try {
                    int vl = Integer.parseInt(elements[0]);
                    if (!perCheckPunishments.containsKey(vl)) {
                        perCheckPunishments.put(vl, new ArrayList<>());
                    }
                    perCheckPunishments.get(vl).add(elements[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            punishmentGroups.put(groupName, new PunishmentGroup(perGroupPunishments, perCheckPunishments));
        }
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
                    break;
                }
                for (int punishVL : group.getPerCheckPunishments().keySet()) {
                    if (vl % punishVL != 0) continue;
                    runPunishment(check, punishVL, group.getPerCheckPunishments());
                    break;
                }
            }
        } else {
            for (PunishmentGroup group : check.getPunishmentGroups()) {
                runPunishment(check, vl, group.getPerGroupPunishments());
                runPunishment(check, vl, group.getPerCheckPunishments());
            }
        }
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
