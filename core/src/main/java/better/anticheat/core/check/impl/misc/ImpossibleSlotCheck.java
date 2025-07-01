package better.anticheat.core.check.impl.misc;

import better.anticheat.core.check.Check;
import better.anticheat.core.check.CheckInfo;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientHeldItemChange;

@CheckInfo(name = "ImpossibleSlot", category = "misc", config = "checks")
public class ImpossibleSlotCheck extends Check {

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.HELD_ITEM_CHANGE) return;
        final WrapperPlayClientHeldItemChange wrapper = new WrapperPlayClientHeldItemChange(event);
        final int slot = wrapper.getSlot();

        // Their slot value also has to be within 0 and 8.
        if (slot < 0 || slot > 8) fail();
    }
}
