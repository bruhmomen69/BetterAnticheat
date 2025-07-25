package better.anticheat.core.punishment;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.configuration.ConfigSection;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class PunishmentManager {

    private final BetterAnticheat plugin;
    private final Map<String, Map<Integer, List<String>>> punishmentGroups = new HashMap<>();
    private final Map<String, Map<Integer, List<String>>> perCheckPunishmentGroups = new HashMap<>();

    public void load() {
        punishmentGroups.clear();
        perCheckPunishmentGroups.clear();
        ConfigSection section = plugin.getFile("settings.yml").getRoot().getConfigSection("punishment-groups");
        if (section == null) {
            return;
        }

        for (ConfigSection groupSection : section.getChildren()) {
            String groupName = groupSection.getKey();
            Map<Integer, List<String>> punishments = new HashMap<>();
            List<String> punishmentList = groupSection.getList(String.class, "per-group-punishments");
            for (String punishment : punishmentList) {
                String[] elements = punishment.split(":", 2);
                try {
                    int vl = Integer.parseInt(elements[0]);
                    if (!punishments.containsKey(vl)) {
                        punishments.put(vl, new ArrayList<>());
                    }
                    punishments.get(vl).add(elements[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            punishmentGroups.put(groupName, punishments);

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
            perCheckPunishmentGroups.put(groupName, perCheckPunishments);
        }
    }

    public Map<Integer, List<String>> getPunishments(String groupName) {
        return punishmentGroups.get(groupName);
    }

    public Map<Integer, List<String>> getPerCheckPunishments(String groupName) {
        return perCheckPunishmentGroups.get(groupName);
    }
}
