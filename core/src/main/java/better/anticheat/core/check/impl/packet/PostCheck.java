package better.anticheat.core.check.impl.packet;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

import java.util.ArrayList;
import java.util.List;

public class PostCheck extends Check {

    private boolean sentFlying = false, login = true;
    private final List<PacketType.Play.Client> post = new ArrayList<>();

    public PostCheck() {
        super("Post");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case PLAYER_FLYING:
            case PLAYER_POSITION:
            case PLAYER_ROTATION:
            case PLAYER_POSITION_AND_ROTATION:
                post.clear();
                sentFlying = true;
                break;
            case CLIENT_TICK_END:
                if (sentFlying && !post.isEmpty()) {
                    fail(post);
                }
                sentFlying = false;
                post.clear();
                break;
            case HELD_ITEM_CHANGE:
                if (login) {
                    login = false;
                    break;
                }
            case ANIMATION:
            case PLAYER_INPUT:
            case ENTITY_ACTION:
            case CLOSE_WINDOW:
            case CREATIVE_INVENTORY_ACTION:
            case EDIT_BOOK:
            case INTERACT_ENTITY:
            case NAME_ITEM:
            case PLAYER_ABILITIES:
            case PLAYER_BLOCK_PLACEMENT:
            case PLAYER_DIGGING:
            case PLUGIN_MESSAGE:
            case SPECTATE:
            case TAB_COMPLETE:
            case UPDATE_SIGN:
                if (sentFlying) post.add(event.getPacketType());
                break;
        }
    }
}
