package better.anticheat.core.check.impl.combat;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

public class ActionInteractOrderCheck extends Check {

    private boolean sentEntityAction = false;

    public ActionInteractOrderCheck() {
        super("ActionInteractOrder");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case CLIENT_TICK_END:
                sentEntityAction = false;
                break;
            case ENTITY_ACTION:
                sentEntityAction = true;
                break;
            case INTERACT_ENTITY:
                // Only run when the player is attacking and when they're actively sending flying packets.
                WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
                if (!wrapper.getAction().equals(WrapperPlayClientInteractEntity.InteractAction.ATTACK)) return;

                // You cannot update an entity action status before you attack.
                if (sentEntityAction) fail();
                break;
        }
    }
}
