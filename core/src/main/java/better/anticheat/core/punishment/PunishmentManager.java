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
    private final Map<String, PunishmentGroup> punishmentGroups = new HashMap<>();

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
}
