package better.anticheat.core.check.impl.heuristic;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;

public class PitchSnapCheck extends Check {

    private boolean rotated = false;
    private int stage = 0;
    private float llDeltaPitch;

    public PitchSnapCheck() {
        super("PitchSnap");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case PLAYER_ROTATION:
            case PLAYER_POSITION_AND_ROTATION:
                rotated = true;
                
                switch (stage) {
                    case 0:
                        stage = 1;
                        break;
                    case 1:
                        llDeltaPitch = Math.abs(player.getRotationTracker().getDeltaPitch());
                        stage = 2;
                        break;
                    case 2:
                        stage = 3;
                        break;
                    case 3:
                        float lDeltaPitch = Math.abs(player.getRotationTracker().getLastDeltaPitch());
                        float deltaPitch = Math.abs(player.getRotationTracker().getDeltaPitch());

                        if (llDeltaPitch < 3f && lDeltaPitch > 15f && deltaPitch < 3f) fail();

                        llDeltaPitch = lDeltaPitch;
                        break;
                }

                break;
            case CLIENT_TICK_END:
                if (!rotated) stage = 0;
                rotated = false;
                break;
            case TELEPORT_CONFIRM:
                stage = 0;
                break;
        }
    }
}
