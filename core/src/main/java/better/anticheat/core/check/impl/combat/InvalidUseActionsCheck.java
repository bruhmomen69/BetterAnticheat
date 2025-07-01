package better.anticheat.core.check.impl.combat;

import better.anticheat.core.check.Check;
import better.anticheat.core.check.CheckInfo;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

@CheckInfo(name = "InvalidUseActions", category = "combat", config = "checks")
public class InvalidUseActionsCheck extends Check {

    private boolean blocking = false, attacked = false, placed = false;

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {

        /*
         * Primarily an auto block check.
         * Rather than checking for blocking in the same tick as the attack, we actively track blocking over longer periods.
         * However, we don't know if the player is actually blocking. That's why it's important that we only flag if the
         * player released their used item... meaning they were blocking.
         */

        switch (event.getPacketType()) {
            case INTERACT_ENTITY:
                WrapperPlayClientInteractEntity interactWrapper = new WrapperPlayClientInteractEntity(event);
                if (interactWrapper.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;
                attacked = true;

                break;
            case PLAYER_BLOCK_PLACEMENT:
                placed = true;
                break;
            case USE_ITEM:

                blocking = true;
                attacked = placed = false;

                break;
            case PLAYER_DIGGING:
                WrapperPlayClientPlayerDigging diggingWrapper = new WrapperPlayClientPlayerDigging(event);
                if (diggingWrapper.getAction() != DiggingAction.RELEASE_USE_ITEM) return;

                if (blocking && attacked) fail("atk");
                else if (blocking && placed) fail("plc");
                blocking = attacked = placed = false;

                break;
        }
    }
}
