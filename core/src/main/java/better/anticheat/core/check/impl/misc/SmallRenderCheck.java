package better.anticheat.core.check.impl.misc;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSettings;

public class SmallRenderCheck extends Check {

    public SmallRenderCheck() {
        super("SmallRender");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.CLIENT_SETTINGS) return;
        WrapperPlayClientSettings wrapper = new WrapperPlayClientSettings(event);

        // The minimum view distance a vanilla player can send is 2.
        if (wrapper.getViewDistance() < 2) fail();
    }
}
