package better.anticheat.core.check.impl.packet;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;

public class LoadOrderCheck extends Check {

    private boolean loaded = false;

    public LoadOrderCheck() {
        super("LoadOrder");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        if (loaded) return;

        switch (event.getPacketType()) {
            case PLAYER_LOADED:
                loaded = true;
                break;
            case ANIMATION:
            case ENTITY_ACTION:
            case CLOSE_WINDOW:
            case CREATIVE_INVENTORY_ACTION:
            case EDIT_BOOK:
            case INTERACT_ENTITY:
            case NAME_ITEM:
            case PLAYER_BLOCK_PLACEMENT:
            case PLAYER_DIGGING:
            case SPECTATE:
            case TAB_COMPLETE:
            case UPDATE_SIGN:
                fail(event.getPacketType());
                break;
        }
    }
}
