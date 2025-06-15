package better.anticheat.core.check.impl.flying;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

public class ImpossiblePositionCheck extends Check {

    public ImpossiblePositionCheck() {
        super("ImpossiblePosition");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        if (!WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) return;
        WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);
        if (!wrapper.hasPositionChanged()) return;

        if (!Double.isFinite(wrapper.getLocation().getX())) fail("x");
        if (!Double.isFinite(wrapper.getLocation().getY())) fail("y");
        if (!Double.isFinite(wrapper.getLocation().getZ())) fail("z");
    }
}
