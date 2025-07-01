package better.anticheat.core.check.impl.combat;

import better.anticheat.core.check.Check;
import better.anticheat.core.check.CheckInfo;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckInfo(name = "ActionInteractOrder", category = "combat", config = "checks")
public class ActionInteractOrderCheck extends Check {

    private boolean sentEntityAction = false;

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {

        /*
         * Minecraft's processing does not allow
         */

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
