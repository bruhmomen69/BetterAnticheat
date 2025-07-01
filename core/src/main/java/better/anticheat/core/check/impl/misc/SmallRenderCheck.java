package better.anticheat.core.check.impl.misc;

import better.anticheat.core.check.Check;
import better.anticheat.core.check.CheckInfo;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSettings;

@CheckInfo(name = "SmallRender", category = "misc", config = "checks")
public class SmallRenderCheck extends Check {

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.CLIENT_SETTINGS) return;
        WrapperPlayClientSettings wrapper = new WrapperPlayClientSettings(event);

        // The minimum view distance a vanilla player can send is 2.
        if (wrapper.getViewDistance() < 2) fail();
    }
}
