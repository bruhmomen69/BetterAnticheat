package better.anticheat.core.punishment;

import lombok.Data;

import java.util.List;
import java.util.Map;
@Data
public class PunishmentGroup {
    private final String name;
    private final int nameHash;
    private final Map<Integer, List<String>> perGroupPunishments;
    private final Map<Integer, List<String>> perCheckPunishments;

    public PunishmentGroup(String name, Map<Integer, List<String>> perGroupPunishments, Map<Integer, List<String>> perCheckPunishments) {
        this.name = name;
        this.nameHash = name.hashCode();
        this.perGroupPunishments = perGroupPunishments;
        this.perCheckPunishments = perCheckPunishments;
    }
}
