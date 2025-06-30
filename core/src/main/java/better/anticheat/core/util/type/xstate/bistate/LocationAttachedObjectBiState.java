package better.anticheat.core.util.type.xstate.bistate;

import com.github.retrooper.packetevents.protocol.world.Location;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class LocationAttachedObjectBiState<A> extends ObjectBiState<A> {
    private final Location location;

    public LocationAttachedObjectBiState(@NotNull A current, Location location) {
        super(current);
        this.location = location;
    }
}
