package better.anticheat.core.check.impl.flying;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;

public class FlyingSequenceCheck extends Check {

    private int ticks = -1;

    public FlyingSequenceCheck() {
        super("FlyingSequence");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case PLAYER_LOADED:
                ticks = 0;
                break;
            case VEHICLE_MOVE:
            case PLAYER_POSITION:
            case PLAYER_POSITION_AND_ROTATION:
                if (ticks < 0) return;
                ticks = 0;
                break;
            case CLIENT_TICK_END:
                // Prevent until first position is sent.
                if (ticks < 0) return;
                if (++ticks > 20) fail(ticks);
                break;
        }
    }
}
