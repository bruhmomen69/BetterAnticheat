package better.anticheat.core.player.tracker.impl.teleport;

import better.anticheat.core.util.type.Pair;
import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

/**
 * This class represents a pending teleport. This includes onGround, position, rotation, and relative movement
 * information.
 */
@Getter
public class Teleport {

    @Setter
    private boolean handled = false;
    private final Vector3d position;
    // Pitch, Yaw.
    private final Pair<Float, Float> rotation;
    private final RelativeFlag relativeFlags;
    private final Boolean onGround;

    /**
     * All params are optional as they can be nulled.
     * Rotation should be (Pitch, Yaw).
     */
    public Teleport(@Nullable Vector3d position, @Nullable Pair<Float, Float> rotation, @Nullable Boolean onGround, @Nullable RelativeFlag relativeFlags) {
        this.position = position;
        this.rotation = rotation;
        this.onGround = onGround;
        this.relativeFlags = relativeFlags;
    }
}
