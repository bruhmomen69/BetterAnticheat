package better.anticheat.core.check.impl.dig;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

public class DigOrderCheck extends Check {

    private boolean started = true, lastCanceled = false;

    public DigOrderCheck() {
        super("DigOrder");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {

        /*
         * Minecraft works very iffy regarding insta break blocks. If you break an insta break block, you send a
         * Start Destroy Block but never a Stop Destroy Block. This means you can have 2 starts in a row. However,
         * Stop Destroy Block requires there to have been a Start Destroy Block sent previously. This means that if you
         * end twice with no start in between you're cheating.
         *
         * This also accounts for an extremely niche piece of weird net code that Nekroses found to false the check.
         * If a client is teleported while digging, it will send a Cancel. However, if it moves the client slightly
         * back in a position where it can still finish the digging it will send the Finish. This means you can
         * technically end twice, but it must go in the order of Start -> Cancelled -> Finished to do so.
         *
         * To account for this if the second ending is a Finish the previous must not be a Cancelled. If the second
         * ending is a Cancelled it doesn't matter what the previous is.
         *
         * Link to Nekroses (as a thank you): https://www.youtube.com/channel/UCyyX_xSdXDcKGDJCY4JqCrA
         *
         * This check just verifies that behavior, possibly patching some nuker or fastbreak cheats.
         */

        if (event.getPacketType() != PacketType.Play.Client.PLAYER_DIGGING) return;
        final WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);

        switch (wrapper.getAction()) {
            case START_DIGGING:
                started = true;
                lastCanceled = false;

                break;
            case CANCELLED_DIGGING:
                if (!started) fail();

                started = false;
                lastCanceled = true;

                break;
            case FINISHED_DIGGING:
                if (!started && !lastCanceled) {
                    fail();
                }

                started = lastCanceled = false;

                break;
        }
    }
}
