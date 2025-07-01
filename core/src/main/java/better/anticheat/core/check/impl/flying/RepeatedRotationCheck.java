package better.anticheat.core.check.impl.flying;

import better.anticheat.core.check.Check;
import better.anticheat.core.check.CheckInfo;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;

@CheckInfo(name = "RepeatedRotation", category = "flying", config = "checks")
public class RepeatedRotationCheck extends Check {

    private boolean loaded = false;
    private int exemptTicks = 0;

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case PLAYER_LOADED:
                loaded = true;
            case VEHICLE_MOVE:
                exemptTicks = 2;
                break;
            // Won't be effected by teleporting since that forces a POS_LOOK packet.
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
