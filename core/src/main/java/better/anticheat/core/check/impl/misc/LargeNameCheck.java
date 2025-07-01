package better.anticheat.core.check.impl.misc;

import better.anticheat.core.check.Check;
import better.anticheat.core.check.CheckInfo;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientNameItem;

@CheckInfo(name = "LargeName", category = "misc", config = "checks")
public class LargeNameCheck extends Check {

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.NAME_ITEM) return;
        final WrapperPlayClientNameItem wrapper = new WrapperPlayClientNameItem(event);

        // As of 1.17, the maximum size is 50.
        if (wrapper.getItemName().length() > 50) fail();
    }
}