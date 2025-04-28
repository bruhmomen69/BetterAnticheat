package better.anticheat.core.check.impl.dig;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

public class NoSwingDigCheck extends Check {

    private boolean started = false, swungThisTick = false, constantSwinging = false;

    public NoSwingDigCheck() {
        super("NoSwingDig");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {

        /*
         * This check just verifies that you swing once a tick during digging.
         */

        switch (event.getPacketType()) {
            case CLIENT_TICK_END:
                if (started && !swungThisTick) constantSwinging = false;
                swungThisTick = false;
                break;
            case ANIMATION:

                if (started) swungThisTick = true;

                break;
            case PLAYER_DIGGING:
                WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);

                // Switches > if statements.
                switch (wrapper.getAction()) {
                    case START_DIGGING:

                        started = true;
                        constantSwinging = true;

                        break;
                    case FINISHED_DIGGING:
                    case CANCELLED_DIGGING:

                        // If a player reaches the end of digging they must have been actively swinging.
                        if (started && !constantSwinging) fail();
                        started = false;

                        break;
                }

                break;
        }
    }
}
