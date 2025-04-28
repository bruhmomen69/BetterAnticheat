package better.anticheat.core.check.impl.combat;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

public class NoSlowCheck extends Check {

    private boolean blocking = false, sprinting = false;

    public NoSlowCheck() {
        super("NoSlow");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case USE_ITEM:
                blocking = true;
                sprinting = false;
                break;
            case ENTITY_ACTION:
                WrapperPlayClientEntityAction actionWrapper = new WrapperPlayClientEntityAction(event);
                if (actionWrapper.getAction().equals(WrapperPlayClientEntityAction.Action.START_SPRINTING)) {
                    sprinting = true;
                }
                break;
            case PLAYER_DIGGING:
                WrapperPlayClientPlayerDigging diggingWrapper = new WrapperPlayClientPlayerDigging(event);
                if (diggingWrapper.getAction() != DiggingAction.RELEASE_USE_ITEM) return;

                // Only flag on use item release as previously we don't know if the player is actually blocking.

                if (blocking && sprinting) fail();
                blocking = false;

                break;
        }
    }
}
