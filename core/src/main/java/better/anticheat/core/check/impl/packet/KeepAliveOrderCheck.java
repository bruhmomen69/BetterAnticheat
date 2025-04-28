package better.anticheat.core.check.impl.packet;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientKeepAlive;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerKeepAlive;

import java.util.ArrayList;
import java.util.List;

public class KeepAliveOrderCheck extends Check {

    private final List<Long> keepAliveIDs = new ArrayList<>();

    public KeepAliveOrderCheck() {
        super("KeepAliveOrder");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.KEEP_ALIVE) return;
        WrapperPlayClientKeepAlive wrapper = new WrapperPlayClientKeepAlive(event);

        if (!keepAliveIDs.isEmpty() && keepAliveIDs.getFirst() == wrapper.getId()) keepAliveIDs.removeFirst();
        else fail();
    }

    @Override
    public void handleSendPlayPacket(PacketPlaySendEvent event) {
        if (event.isCancelled()) return;
        if (event.getPacketType() != PacketType.Play.Server.KEEP_ALIVE) return;
        WrapperPlayServerKeepAlive wrapper = new WrapperPlayServerKeepAlive(event);
        keepAliveIDs.add(wrapper.getId());
    }
}
