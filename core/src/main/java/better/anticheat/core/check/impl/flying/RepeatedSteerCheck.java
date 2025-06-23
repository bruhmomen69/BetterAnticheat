package better.anticheat.core.check.impl.flying;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

public class RepeatedSteerCheck extends Check {

    private boolean lastRotating = true;

    public RepeatedSteerCheck() {
        super("RepeatedSteer");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        /*
         * The idea here is that a player cannot send 2 Steer Vehicle packets without a rotation in between.
         * This may be able to identify simple vehicle cheats.
         */

        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);
            if (wrapper.hasRotationChanged()) lastRotating = true;
        } else if (event.getPacketType() == PacketType.Play.Client.VEHICLE_MOVE) {
            //There has to have been a rotation in between the last steer vehicle packet.
            if (!lastRotating) {
                fail();
            }

            lastRotating = false;
        }
    }
}
