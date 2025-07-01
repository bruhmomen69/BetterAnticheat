package better.anticheat.core.check.impl.combat;

import better.anticheat.core.check.Check;
import better.anticheat.core.check.CheckInfo;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

@CheckInfo(name = "DualClick", category = "combat", config = "checks")
public class DualClickCheck extends Check {

    private boolean leftClickAtk = false, leftClickDig = false, rightClickUse = false, rightClickPlace = false;

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case CLIENT_TICK_END:
                if (leftClickDig && (rightClickPlace || rightClickUse)) fail("atk");
                else if (leftClickAtk && rightClickPlace) fail("dig");
                leftClickAtk = leftClickDig = rightClickPlace = rightClickUse = false;
                break;
            case PLAYER_BLOCK_PLACEMENT:
                rightClickPlace = true;
                break;
            case USE_ITEM:
                rightClickUse = true;
                break;
            case PLAYER_DIGGING:
                WrapperPlayClientPlayerDigging digWrapper = new WrapperPlayClientPlayerDigging(event);
                // Can false on instant break blocks because Mojang.
                switch (digWrapper.getAction()) {
                    case CANCELLED_DIGGING:
                    case FINISHED_DIGGING:
                    case RELEASE_USE_ITEM:
                        break;
                    default:
                        return;
                }
                leftClickDig = true;
                break;
            case INTERACT_ENTITY:
                WrapperPlayClientInteractEntity interactWrapper = new WrapperPlayClientInteractEntity(event);
                if (!interactWrapper.getAction().equals(WrapperPlayClientInteractEntity.InteractAction.ATTACK)) return;
                leftClickAtk = true;
                break;
        }
    }
}
