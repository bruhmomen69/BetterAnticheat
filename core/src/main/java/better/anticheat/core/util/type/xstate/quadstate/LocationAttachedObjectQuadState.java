package better.anticheat.core.util.type.xstate.quadstate;

import com.github.retrooper.packetevents.protocol.world.Location;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class LocationAttachedObjectQuadState<A> extends ObjectQuadState<A> {
    private final Location location;

    public LocationAttachedObjectQuadState(@NotNull A current, Location location) {
        super(current);
        this.location = location;
    }
}
