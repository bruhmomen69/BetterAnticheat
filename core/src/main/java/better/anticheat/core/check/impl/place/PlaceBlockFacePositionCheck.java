package better.anticheat.core.check.impl.place;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

public class PlaceBlockFacePositionCheck extends Check {

    private Vector3d position = null;

    public PlaceBlockFacePositionCheck() {
        super("PlaceBlockFacePosition");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);
            if (!wrapper.hasPositionChanged()) return;
            position = wrapper.getLocation().getPosition();
        } else if (event.getPacketType() != PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) return;

        WrapperPlayClientPlayerBlockPlacement placeWrapper = new WrapperPlayClientPlayerBlockPlacement(event);
        Vector3i blockPos = placeWrapper.getBlockPosition();
        switch (placeWrapper.getFace()) {
            case OTHER:
                if ((blockPos.getX() != -1) && (blockPos.getY() != 4095) && (blockPos.getZ() != -1)) fail();
                break;
            case NORTH:
                if ((blockPos.getZ() + 1.03) < position.getZ()) fail();
                break;
            case SOUTH:
                if ((blockPos.getZ() - 0.03) > position.getZ()) fail();
                break;
            case WEST:
                if ((blockPos.getX() + 1.03) < position.getX()) fail();
                break;
            case EAST:
                if ((blockPos.getX() - 0.03) > position.getX()) fail();
                break;
            case DOWN:
                if ((position.getY() - blockPos.getY()) >= 1) fail();
                break;
        }
    }
}
