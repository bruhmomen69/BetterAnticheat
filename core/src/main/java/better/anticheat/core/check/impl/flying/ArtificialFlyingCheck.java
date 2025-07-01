package better.anticheat.core.check.impl.flying;

import better.anticheat.core.check.Check;
import better.anticheat.core.check.CheckInfo;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;

@CheckInfo(name = "ArtificialFlying", category = "flying", config = "checks")
public class ArtificialFlyingCheck extends Check {

    private boolean sentFlying = false, teleported = true;

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        /*
         * Now that Mojang removed their use item desync patch, it is impossible to send multiple flying packets in a
         * tick. This could indicate a badly designed movement/rotation alteration.
         */
        switch (event.getPacketType()) {
            case PLAYER_FLYING:
            case PLAYER_POSITION:
            case PLAYER_ROTATION:
            case PLAYER_POSITION_AND_ROTATION:
                if (teleported) break;
                if (sentFlying) fail();
                sentFlying = true;
                break;
            case CLIENT_TICK_END:
                sentFlying = teleported = false;
                break;
            case TELEPORT_CONFIRM:
                teleported = true;
                break;
        }
    }
}
