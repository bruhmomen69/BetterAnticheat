package better.anticheat.core.check.impl.combat;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

public class InvalidReleaseValuesCheck extends Check {

    public InvalidReleaseValuesCheck() {
        super("InvalidReleaseValues");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.PLAYER_DIGGING) return;
        WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);

        if (wrapper.getAction().equals(DiggingAction.RELEASE_USE_ITEM)) {

            /*
             * Release packets have defined inputs that should never be different:
             * BlockFace = DOWN
             * X = 0
             * Y = 0
             * Z = 0
             */

            if (
                    !wrapper.getBlockFace().equals(BlockFace.DOWN) ||
                    wrapper.getBlockPosition().getX() != 0 ||
                    wrapper.getBlockPosition().getY() != 0 ||
                    wrapper.getBlockPosition().getZ() != 0
            ) fail();
        }
    }
}
