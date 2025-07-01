package better.anticheat.core.check.impl.combat;

import better.anticheat.core.check.Check;
import better.anticheat.core.check.CheckInfo;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;

@CheckInfo(name = "MultipleAction", category = "combat", config = "checks")
public class MultipleActionCheck extends Check {

    private boolean changedSneak = false, changedSprint = false;

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case CLIENT_TICK_END:
                changedSneak = changedSprint = false;
                break;
            case ENTITY_ACTION:
                WrapperPlayClientEntityAction wrapper = new WrapperPlayClientEntityAction(event);

                switch (wrapper.getAction()) {
                    case START_SNEAKING:
                    case STOP_SNEAKING:
                        if (changedSneak) fail();
                        changedSneak = true;
                        break;
                    case START_SPRINTING:
                    case STOP_SPRINTING:
                        if (changedSprint) fail();
                        changedSprint = true;
                        break;
                }
                break;
        }
    }
}
