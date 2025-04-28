package better.anticheat.core.check.impl.position;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

public class ImpossibleRotationCheck extends Check {

    public ImpossibleRotationCheck() {
        super("ImpossibleRotation");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        if (!WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) return;
        WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);
        if (!wrapper.hasRotationChanged()) return;

        // Rotations must be real numbers and not exceed boundaries.

        final boolean invalidPitch = checkForInvalid(wrapper.getLocation().getPitch(), true);
        final boolean invalidYaw = checkForInvalid(wrapper.getLocation().getYaw(), false);

        if (invalidPitch || invalidYaw) fail();
    }

    private boolean checkForInvalid(float rotation, boolean checkPitch) {
        if (!Float.isFinite(rotation)) return true;
        else return (checkPitch && Math.abs(rotation) > 90);
    }
}
