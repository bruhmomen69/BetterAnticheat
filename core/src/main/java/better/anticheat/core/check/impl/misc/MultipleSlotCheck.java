package better.anticheat.core.check.impl.misc;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;

public class MultipleSlotCheck extends Check {

    private boolean change = false;

    public MultipleSlotCheck() {
        super("MultipleSlot");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case SLOT_STATE_CHANGE:
                if (change) fail();
                change = true;
                break;
            case CLIENT_TICK_END:
                change = false;
                break;
        }
    }
}