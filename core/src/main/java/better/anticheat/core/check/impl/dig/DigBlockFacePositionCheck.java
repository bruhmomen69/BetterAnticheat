package better.anticheat.core.check.impl.dig;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

public class DigBlockFacePositionCheck extends Check {

    private Vector3d position = null;

    public DigBlockFacePositionCheck() {
        super("DigBlockFacePosition");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);
            if (!wrapper.hasPositionChanged()) return;
            position = wrapper.getLocation().getPosition();
            return;
        } else if (event.getPacketType() != PacketType.Play.Client.PLAYER_DIGGING) return;

        WrapperPlayClientPlayerDigging digWrapper = new WrapperPlayClientPlayerDigging(event);
        Vector3i blockPos = digWrapper.getBlockPosition();
        switch (digWrapper.getBlockFace()) {
            case NORTH:
                if ((blockPos.getZ() + 1.03) < position.getZ()) fail(digWrapper.getBlockFace() + " " + digWrapper.getAction());
                break;
            case SOUTH:
                if ((blockPos.getZ() - 0.03) > position.getZ()) fail(digWrapper.getBlockFace() + " " + digWrapper.getAction());
                break;
            case WEST:
                if ((blockPos.getX() + 1.03) < position.getX()) fail(digWrapper.getBlockFace() + " " + digWrapper.getAction());
                break;
            case EAST:
                if ((blockPos.getX() - 0.03) > position.getX()) fail(digWrapper.getBlockFace() + " " + digWrapper.getAction());
                break;
            case DOWN:
                if ((position.getY() - blockPos.getY()) >= 1) fail(digWrapper.getBlockFace() + " " + digWrapper.getAction());
                break;
        }
    }
}