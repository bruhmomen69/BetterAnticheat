package better.anticheat.core.player;

import better.anticheat.core.check.Check;
import lombok.Data;

@Data
public class Violation {
    private final Check check;
    private final String groupName;
    private final long creationTime;
    private final int vl;
}
