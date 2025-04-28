package better.anticheat.core.check.impl.combat;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

public class NoSwingCombatCheck extends Check {

    private boolean swung = true;

    public NoSwingCombatCheck() {
        super("NoSwingCombat");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case ANIMATION:
                swung = true;

                break;
            case INTERACT_ENTITY:
                WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
                if (wrapper.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;

                if (!swung) fail();

                swung = false;
                break;
        }
    }
}
