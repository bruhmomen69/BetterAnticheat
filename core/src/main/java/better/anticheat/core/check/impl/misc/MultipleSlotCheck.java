package better.anticheat.core.check.impl.misc;

import better.anticheat.core.check.Check;
import better.anticheat.core.check.CheckInfo;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;

@CheckInfo(name = "MultipleSlot", category = "misc", config = "checks")
public class MultipleSlotCheck extends Check {

    private boolean change = false;

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