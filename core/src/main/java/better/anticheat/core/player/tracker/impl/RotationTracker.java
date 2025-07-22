package better.anticheat.core.player.tracker.impl;

import better.anticheat.core.player.Player;
import better.anticheat.core.player.tracker.Tracker;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import lombok.Getter;

/**
 * This tracker handles basic data tracking regarding a player's rotation. This is a centralized way to access a
 * player's latest rotation at any given time.
 */
@Getter
public class RotationTracker extends Tracker {

    private boolean rotation, lastRotation;
    private float pitch, yaw, lastPitch, lastYaw;
    private float deltaPitch, deltaYaw, lastDeltaPitch, lastDeltaYaw;

    public RotationTracker(Player player) {
        super(player);
    }

    /*
     * Packet handling.
     */

    @Override
    public void handlePacketPlayReceive(PacketPlayReceiveEvent event) {
        if (!WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) return;
        WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);

        lastRotation = rotation;
        rotation = wrapper.hasRotationChanged();
        if (!rotation) return;

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
