package better.anticheat.core.check.impl.dig;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.check.Check;
import better.anticheat.core.check.CheckInfo;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

/**
 * This check looks to see if a block face is dug that is mathematically impossible to see.
 */
@CheckInfo(name = "DigBlockFacePosition", category = "dig")
public class DigBlockFacePositionCheck extends Check {

    private Vector3d position = null;

    public DigBlockFacePositionCheck(BetterAnticheat plugin) {
        super(plugin);
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {

        /*
         * When working with block digs, one important insight that the client informs the server of is the side of the
         * block that was interacted with. Using this information, we can use the player's position to determine whether
         * it was physically possible for the player to have seen the side that they placed a block against.
         *
         * This also includes a horrifically simple patch for 2.0E-4 (see ArtificialPositionCheck for an explanation of
         * what that means) where we just give that much leniency for any interaction. If it works, it works!
         */

        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);
            if (!wrapper.hasPositionChanged()) return;
            position = wrapper.getLocation().getPosition();
            return;
        } else if (event.getPacketType() != PacketType.Play.Client.PLAYER_DIGGING) return;

        WrapperPlayClientPlayerDigging digWrapper = new WrapperPlayClientPlayerDigging(event);
        switch (digWrapper.getAction()) {
            case FINISHED_DIGGING:
            case START_DIGGING:
                break;
            default:
                return;
        }

        Vector3i blockPos = digWrapper.getBlockPosition();
        switch (digWrapper.getBlockFace()) {
            case OTHER:
                if (blockPos.getX() != -1 || blockPos.getY() != 4095 || blockPos.getZ() != -1) fail("other");
                break;
            case NORTH:
                if ((blockPos.getZ() + 1 + 2.0E-4) < position.getZ()) fail(digWrapper.getBlockFace() + " " + digWrapper.getAction());
                break;
            case SOUTH:
                if ((blockPos.getZ() - 2.0E-4) > position.getZ()) fail(digWrapper.getBlockFace() + " " + digWrapper.getAction());
                break;
            case WEST:
                if ((blockPos.getX() + 1 + 2.0E-4) < position.getX()) fail(digWrapper.getBlockFace() + " " + digWrapper.getAction());
                break;
            case EAST:
                if ((blockPos.getX() - 2.0E-4) > position.getX()) fail(digWrapper.getBlockFace() + " " + digWrapper.getAction());
                break;
            case DOWN:
                if (position.getY() >= blockPos.getY()) fail(digWrapper.getBlockFace() + " " + digWrapper.getAction());
                break;
        }
    }
}