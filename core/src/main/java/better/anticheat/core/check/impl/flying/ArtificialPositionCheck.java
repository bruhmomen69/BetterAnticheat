package better.anticheat.core.check.impl.flying;

import better.anticheat.core.check.Check;
import better.anticheat.core.check.CheckInfo;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;

@CheckInfo(name = "ArtificialPosition", category = "flying", config = "checks")
public class ArtificialPositionCheck extends Check {

    private boolean position = true;
    private int ticks = -1, samePositions = 0;

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case PLAYER_LOADED:
                ticks = samePositions = 0;
                break;
            case TELEPORT_CONFIRM:
                if (ticks < 0) break;
                ticks = samePositions = 0;
                break;
            case CLIENT_TICK_END:
                if (ticks < 0) break;
                if (++ticks == 20) {
                    if (!position) fail("Lack");
                    ticks = samePositions = 0;
                    position = false;
                }
                break;
            case PLAYER_POSITION:
            case PLAYER_POSITION_AND_ROTATION:
                position = true;

                final double deltaX = player.getPositionTracker().getDeltaX();
                final double deltaY = player.getPositionTracker().getDeltaY();
                final double deltaZ = player.getPositionTracker().getDeltaZ();

                /*
                 * Value is (2.0E-4)^2 in 1.18.2+, (0.03)^2 in 1.18.1-.
                 */
                if (deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ <= 4.0E-8D) {
                    if (++samePositions > 1) fail("Excess");
                }

                break;
        }
    }
}
