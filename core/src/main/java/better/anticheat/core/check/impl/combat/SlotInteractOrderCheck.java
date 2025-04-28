package better.anticheat.core.check.impl.combat;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;

public class SlotInteractOrderCheck extends Check {

    private boolean slotChange = false;

    public SlotInteractOrderCheck() {
        super("SlotInteractOrder");
    }

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
