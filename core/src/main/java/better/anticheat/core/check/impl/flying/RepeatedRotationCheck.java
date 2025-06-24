package better.anticheat.core.check.impl.flying;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;

public class RepeatedRotationCheck extends Check {

    private boolean loaded = false;
    private int exemptTicks = 0;

    public RepeatedRotationCheck() {
        super("RepeatedRotation");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case PLAYER_LOADED:
                loaded = true;
            case VEHICLE_MOVE:
                exemptTicks = 2;
                break;
            case PLAYER_ROTATION:
                if (exemptTicks > 0) break;
                if (!loaded) return;
                if (player.getRotationTracker().getDeltaPitch() == 0 && player.getRotationTracker().getDeltaYaw() == 0) fail();
            case CLIENT_TICK_END:
                exemptTicks--;
                break;
        }
    }
}
