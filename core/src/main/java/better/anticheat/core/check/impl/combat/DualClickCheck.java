package better.anticheat.core.check.impl.combat;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

public class DualClickCheck extends Check {

    private boolean leftCLick = false, rightClick = false;

    public DualClickCheck() {
        super("DualClick");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case CLIENT_TICK_END:
                if (leftCLick && rightClick) fail();
                leftCLick = rightClick = false;
                break;
            case PLAYER_BLOCK_PLACEMENT:
            case USE_ITEM:
                rightClick = true;
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
                leftCLick = true;
                break;
            case INTERACT_ENTITY:
                WrapperPlayClientInteractEntity interactWrapper = new WrapperPlayClientInteractEntity(event);
                if (!interactWrapper.getAction().equals(WrapperPlayClientInteractEntity.InteractAction.ATTACK)) return;
                leftCLick = true;
                break;
        }
    }
}
