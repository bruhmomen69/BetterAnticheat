package better.anticheat.core.check.impl.dig;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

public class RepeatedDigCheck extends Check {

    private boolean dug = false;

    public RepeatedDigCheck() {
        super("RepeatedDig");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case CLIENT_TICK_END:
                dug = false;
                break;
            case PLAYER_DIGGING:
                WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);

                switch (wrapper.getAction()) {
                    case START_DIGGING:
                    case FINISHED_DIGGING:
                    case CANCELLED_DIGGING:
                        break;
                    default:
                        return;
                }

                if (dug) fail();
                dug = true;

                break;
        }
    }
}
