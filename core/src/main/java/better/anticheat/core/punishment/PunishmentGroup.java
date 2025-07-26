package better.anticheat.core.punishment;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class PunishmentGroup {
    private final String name;
    private final Map<Integer, List<String>> perGroupPunishments;
    private final Map<Integer, List<String>> perCheckPunishments;
    private final Map<UUID, AtomicInteger> groupViolations = new ConcurrentHashMap<>();
}
