package better.anticheat.core.player.tracker.impl;

import better.anticheat.core.player.Player;
import better.anticheat.core.player.tracker.Tracker;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

public class RotationTracker extends Tracker {

    private float pitch, yaw, lastPitch, lastYaw;
    private float deltaPitch, deltaYaw, lastDeltaPitch, lastDeltaYaw;

    public RotationTracker(Player player) {
        super(player);
    }

    /*
     * Getters.
     */

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public float getLastPitch() {
        return lastPitch;
    }

    public float getLastYaw() {
        return lastYaw;
    }

    public float getDeltaPitch() {
        return deltaPitch;
    }

    public float getDeltaYaw() {
        return deltaYaw;
    }

    public float getLastDeltaPitch() {
        return lastDeltaPitch;
    }

    public float getLastDeltaYaw() {
        return lastDeltaYaw;
    }

    /*
     * Packet handling.
     */

    @Override
    public void handlePacketPlayReceive(PacketPlayReceiveEvent event) {
        if (!WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) return;
        WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);
        if (!wrapper.hasRotationChanged()) return;

        lastPitch = pitch;
        lastYaw = yaw;
        lastDeltaPitch = deltaPitch;
        lastDeltaYaw = deltaYaw;

        pitch = wrapper.getLocation().getPitch();
        yaw = wrapper.getLocation().getYaw();
        deltaPitch = pitch - lastPitch;
        deltaYaw = yaw - lastYaw;
    }
}
