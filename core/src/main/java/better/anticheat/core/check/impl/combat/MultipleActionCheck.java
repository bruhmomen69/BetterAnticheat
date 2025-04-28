package better.anticheat.core.check.impl.combat;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;

public class MultipleActionCheck extends Check {

    private boolean changedSneak = false, changedSprint = false;

    public MultipleActionCheck() {
        super("MultipleAction");
    }

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
