package better.anticheat.core.check.impl.heuristic;

import better.anticheat.core.check.Check;
import better.anticheat.core.check.CheckInfo;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "CombatAcceleration", category = "heuristic", config = "checks")
public class CombatAccelerationCheck extends Check {

    private boolean posRotChange = false, lastTickChange = false, teleporting = false;
    private int ticksSinceAttack = 0;

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case PLAYER_FLYING:
            case PLAYER_POSITION:
            case PLAYER_ROTATION:
            case PLAYER_POSITION_AND_ROTATION:
                WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);

                if (!wrapper.hasPositionChanged() || !wrapper.hasRotationChanged()) break;
                posRotChange = true;
                if (teleporting) break;
                if (!lastTickChange) break;
                if (player.getPositionTracker().getDeltaXZ() <= 0.15) break;

                final double deltaYaw = Math.abs(player.getRotationTracker().getDeltaYaw());
                final double deltaPitch = Math.abs(player.getRotationTracker().getDeltaPitch());
                final double accelerationXZ = Math.abs(player.getPositionTracker().getDeltaXZ() -
                        player.getPositionTracker().getLastDeltaXZ());

                // Literally just a magic value. I'm not sure why it works so well but it does.
                final double accelLimit = (deltaYaw / deltaPitch / 2000);
                if (accelerationXZ < accelLimit && deltaYaw > 15 && deltaPitch > 5 && ticksSinceAttack <= 2) fail();
                break;
            case INTERACT_ENTITY:
                ticksSinceAttack = 0;
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