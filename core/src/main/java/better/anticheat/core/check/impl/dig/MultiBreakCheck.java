package better.anticheat.core.check.impl.dig;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

public class MultiBreakCheck extends Check {

    private boolean hasStarted = false;
    private Vector3i latestStartPosition;

    public MultiBreakCheck() {
        super("MultiBreak");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {

        /*
         * This check patches a very interesting bypass mechanic in some cheats.
         *
         * Some clients bypass classic fast break checks by following this process:
         * Start1 -> Start2 -> Finish1 -> Start3 -> Finish2 -> Start4 -> etc
         *
         * It starts digging multiple blocks at the same time, waiting until the proper time has passed to finish any.
         *
         * This also isn't easily detectable by checking if the last action and current action are starts because the
         * vanilla client does that when breaking insta-break blocks.
         *
         * This doesn't seem to be as common anymore, but I'm still including it regardless.
         */

        if (event.getPacketType() != PacketType.Play.Client.PLAYER_DIGGING) return;
        WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);

        switch (wrapper.getAction()) {
            case START_DIGGING:

                latestStartPosition = wrapper.getBlockPosition();
                hasStarted = true;
                break;
            case FINISHED_DIGGING:
                if (hasStarted) {
                    if (!wrapper.getBlockPosition().equals(latestStartPosition)) fail();
                }

                break;
        }
    }
}
