package better.anticheat.core.player.tracker.impl.entity.type;

import better.anticheat.core.util.type.incrementer.IntIncrementer;
import lombok.Data;

@Data
public class SplitEntityUpdate {
    private final IntIncrementer flyings = new IntIncrementer(0);
    private final EntityData data;
    private final EntityTrackerState oldState;
    private final double x;
    private final double y;
    private final double z;
}
