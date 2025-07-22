package better.anticheat.core.player.tracker.impl.teleport;

import better.anticheat.core.util.type.Pair;
import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

/**
 * This class represents a pending teleport. This includes onGround, position, rotation, and relative movement
 * information.
 */
@Getter
@RequiredArgsConstructor
public class Teleport {

    @Setter
    private boolean handled = false;
    private final @Nullable Vector3d position;
    // Pitch, Yaw.
    private final @Nullable Pair<Float, Float> rotation;
    private final @Nullable RelativeFlag relativeFlags;
    private final @Nullable Boolean onGround;
}
