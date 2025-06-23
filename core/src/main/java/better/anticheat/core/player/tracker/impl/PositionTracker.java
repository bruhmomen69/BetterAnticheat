package better.anticheat.core.player.tracker.impl;

import better.anticheat.core.player.Player;
import better.anticheat.core.player.tracker.Tracker;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

public class PositionTracker extends Tracker {

    private double x, y, z, lastX, lastY, lastZ;
    private double deltaX, deltaY, deltaZ, deltaXZ, lastDeltaX, lastDeltaY, lastDeltaZ, lastDeltaXZ;

    public PositionTracker(Player player) {
        super(player);
    }

    /*
     * Getters.
     */

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getLastX() {
        return lastX;
    }

    public double getLastY() {
        return lastY;
    }

    public double getLastZ() {
        return lastZ;
    }

    public double getDeltaX() {
        return deltaX;
    }

    public double getDeltaY() {
        return deltaY;
    }

    public double getDeltaZ() {
        return deltaZ;
    }

    public double getDeltaXZ() {
        return deltaXZ;
    }

    public double getLastDeltaX() {
        return lastDeltaX;
    }

    public double getLastDeltaY() {
        return lastDeltaY;
    }

    public double getLastDeltaZ() {
        return lastDeltaZ;
    }

    public double getLastDeltaXZ() {
        return lastDeltaXZ;
    }

    /*
     * Packet handling.
     */

    @Override
    public void handlePacketPlayReceive(PacketPlayReceiveEvent event) {
        if (!WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) return;
        WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);
        if (!wrapper.hasPositionChanged()) return;

        lastX = x;
        lastY = y;
        lastZ = z;
        lastDeltaX = deltaX;
        lastDeltaY = deltaY;
        lastDeltaZ = deltaZ;
        lastDeltaXZ = deltaXZ;

        x = wrapper.getLocation().getX();
        y = wrapper.getLocation().getY();
        z = wrapper.getLocation().getZ();
        deltaX = x - lastX;
        deltaY = y - lastY;
        deltaZ = z - lastZ;
        deltaXZ = Math.hypot(deltaX, deltaZ);
    }
}
