package better.anticheat.core.check.impl.misc;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.check.Check;
import better.anticheat.core.check.CheckInfo;
import better.anticheat.core.check.ClientFeatureRequirement;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;

/**
 * This check looks for excessive slot changes in a tick.
 */
@CheckInfo(name = "MultipleSlot", category = "misc", requirements = ClientFeatureRequirement.CLIENT_TICK_END)
public class MultipleSlotCheck extends Check {

    private boolean change = false;

    public MultipleSlotCheck(BetterAnticheat plugin) {
        super(plugin);
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {

        // A client can only switch their hot bar slot once within each tick.

        switch (event.getPacketType()) {
            case SLOT_STATE_CHANGE:
                if (change) fail();
                change = true;
                break;
            case CLIENT_TICK_END:
                change = false;
                break;
        }
    }
}