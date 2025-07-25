package better.anticheat.core.punishment;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PunishmentGroup {
    private final Map<Integer, List<String>> perGroupPunishments;
    private final Map<Integer, List<String>> perCheckPunishments;
}
