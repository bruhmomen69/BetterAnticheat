package better.anticheat.core.check.impl.combat;

import better.anticheat.core.check.Check;
import better.anticheat.core.check.CheckInfo;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckInfo(name = "NoSwingCombat", category = "combat", config = "checks")
public class NoSwingCombatCheck extends Check {

    private boolean swung = true;

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
