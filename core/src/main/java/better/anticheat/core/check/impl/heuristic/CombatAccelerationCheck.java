package better.anticheat.core.check.impl.heuristic;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

public class CombatAccelerationCheck extends Check {

    private boolean posRotChange = false, lastTickChange = false, teleporting = false;
    private double lastX, lastZ, lastDeltaXZ;
    private float lastPitch, lastYaw;
    private int ticksSinceAttack = 0;

    public CombatAccelerationCheck() {
        super("CombatAcceleration");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {

        /*
         * This check is largely messy because we don't have a central rotation and position tracker. It REALLY might be
         * time to add something like that if more heuristic checks similar to this are planned. While the current scope
         * of the anticheat is focusing on checks so basic that it'd be a waste of ram to have something like that, this
         * seems like the direction the anticheat may head later on.
         */

        switch (event.getPacketType()) {
            case INTERACT_ENTITY:
                ticksSinceAttack = 0;
                break;
            case PLAYER_FLYING:
            case PLAYER_POSITION:
            case PLAYER_ROTATION:
            case PLAYER_POSITION_AND_ROTATION:
                WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);
                double accelerationXZ = 0, deltaXZ = 0;
                float deltaPitch = 0, deltaYaw = 0;

                if (wrapper.hasPositionChanged()) {
                    double deltaX = wrapper.getLocation().getX() - lastX;
                    double deltaZ = wrapper.getLocation().getZ() - lastZ;
                    deltaXZ = Math.hypot(deltaX, deltaZ);
                    accelerationXZ = Math.abs(deltaXZ - lastDeltaXZ);
                    lastDeltaXZ = deltaXZ;
                    lastX = wrapper.getLocation().getX();
                    lastZ = wrapper.getLocation().getZ();
                }

                if (wrapper.hasRotationChanged()) {
                    deltaPitch = Math.abs(wrapper.getLocation().getPitch() - lastPitch);
                    deltaYaw = Math.abs(wrapper.getLocation().getYaw() - lastYaw);
                    lastPitch = wrapper.getLocation().getPitch();
                    lastYaw = wrapper.getLocation().getYaw();
                }

                if (!wrapper.hasPositionChanged() || !wrapper.hasRotationChanged()) break;
                posRotChange = true;
                if (teleporting) break;
                if (!lastTickChange) break;
                if (deltaXZ <= 0.15) break;

                // Literally just a magic value. I'm not sure why it works so well but it does.
                final double accelLimit = (deltaYaw / deltaPitch / 2000);
                if (accelerationXZ < accelLimit && deltaYaw > 15 && deltaPitch > 5 && ticksSinceAttack <= 2) fail();
                break;
            case TELEPORT_CONFIRM:
                teleporting = true;
                break;
            case CLIENT_TICK_END:
                lastTickChange = posRotChange;
                posRotChange = teleporting = false;
                ticksSinceAttack = Math.min(5, ++ticksSinceAttack);
                break;
        }
    }
}