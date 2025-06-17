package better.anticheat.core.check.impl.heuristic;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

public class PitchSnapCheck extends Check {

    private boolean rotated = false;
    private int stage = 0;
    private float lllPitch, llPitch, lPitch;

    public PitchSnapCheck() {
        super("PitchSnap");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case PLAYER_ROTATION:
            case PLAYER_POSITION_AND_ROTATION:
                rotated = true;

                WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);

                switch (stage) {
                    case 0:
                        lllPitch = wrapper.getLocation().getPitch();
                        break;
                    case 1:
                        llPitch = wrapper.getLocation().getPitch();
                        break;
                    case 2:
                        lPitch = wrapper.getLocation().getPitch();
                        break;
                    case 3:
                        float llDeltaPitch = Math.abs(llPitch - lllPitch);
                        float lDeltaPitch = Math.abs(lPitch - llPitch);
                        float deltaPitch = Math.abs(wrapper.getLocation().getPitch() - lPitch);

                        if (llDeltaPitch < 3f && lDeltaPitch > 15f && deltaPitch < 3f) fail();

                        lllPitch = llPitch;
                        llPitch = lPitch;
                        lPitch = wrapper.getLocation().getPitch();
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
