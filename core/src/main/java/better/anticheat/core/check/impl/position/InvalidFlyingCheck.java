package better.anticheat.core.check.impl.position;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

public class InvalidFlyingCheck extends Check {

    private boolean lastOnGround;

    public InvalidFlyingCheck() {
        super("InvalidFlying");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        if (!WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) return;
        WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);

        if (!wrapper.hasPositionChanged() && !wrapper.hasRotationChanged()) {
            // For a 1.9+ client to send a regular flying packet there must be a onGround status change.
            if (wrapper.isOnGround() == lastOnGround) fail();
        }

        lastOnGround = wrapper.isOnGround();
    }
}
