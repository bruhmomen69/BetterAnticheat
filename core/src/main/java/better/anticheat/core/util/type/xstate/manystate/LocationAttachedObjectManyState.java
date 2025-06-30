package better.anticheat.core.util.type.xstate.manystate;

import com.github.retrooper.packetevents.protocol.world.Location;
import lombok.*;

@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class LocationAttachedObjectManyState<A> extends ObjectManyState<A> {
    private final Location location;

    public LocationAttachedObjectManyState(int capacity, Location location) {
        super(capacity);
        this.location = location;
    }
}
