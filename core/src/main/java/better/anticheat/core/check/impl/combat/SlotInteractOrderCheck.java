package better.anticheat.core.check.impl.combat;

import better.anticheat.core.check.Check;
import better.anticheat.core.check.CheckInfo;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;

@CheckInfo(name = "SlotInteractOrder", category = "combat", config = "checks")
public class SlotInteractOrderCheck extends Check {

    private boolean slotChange = false;

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case CLIENT_TICK_END:
                slotChange = false;
                break;
            case HELD_ITEM_CHANGE:
                slotChange = true;
                break;
            case INTERACT_ENTITY:
                if (slotChange) fail();
                break;
        }
    }
}
