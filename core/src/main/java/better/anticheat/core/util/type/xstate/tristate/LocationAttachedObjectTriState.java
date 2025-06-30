package better.anticheat.core.util.type.xstate.tristate;

import com.github.retrooper.packetevents.protocol.world.Location;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class LocationAttachedObjectTriState<A> extends ObjectTriState<A> {
    private final Location location;

    public LocationAttachedObjectTriState(@NotNull A current, Location location) {
        super(current);
        this.location = location;
    }
}
