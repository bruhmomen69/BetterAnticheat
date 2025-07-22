package better.anticheat.core.player.tracker.impl.teleport;

import better.anticheat.core.player.Player;
import better.anticheat.core.player.tracker.Tracker;
import better.anticheat.core.util.type.Pair;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * This tracker compensates teleportation packets and checks whether a player's movement matches an eligible teleport.
 */
public class TeleportTracker extends Tracker {

    private final List<Teleport> eligibleTeleports = new ArrayList<>(), remove = new ArrayList<>();

    @Getter
    private boolean teleported = true, positionTeleported = true, rotationTeleported = true, missedTeleport = false;

    public TeleportTracker(Player player) {
        super(player);
    }

    @Override
    public void handlePacketPlayReceive(PacketPlayReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CLIENT_TICK_END) {
            positionTeleported = rotationTeleported = missedTeleport = false;

            /*
             * Due to the current design of our confirmation system, teleports do not actually resolve within our
             * confirmation window. Rather, they resolve in the tick following the post transaction (apart from world
             * change teleports! Which resolve in time for some reason?). So, we handle it here in the following tick if
             * needed.
             */
            for (Teleport teleport : remove) {
                eligibleTeleports.remove(teleport);
                if (!teleport.isHandled()) missedTeleport = true;
            }
            remove.clear();
        }

        if (!WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) return;
        WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);

        for (Teleport teleport : eligibleTeleports) {
            if (match(teleport, wrapper)) {
                if (teleport.getPosition() != null) positionTeleported = true;
                if (teleport.getRotation() != null) rotationTeleported = true;
                teleported = true;
                teleport.setHandled(true);
            }

            /*
             * Players receive teleports sequentially, so if they haven't handled this teleport yet then they can't
             * handle any future ones.
             * Another intended feature: we allow players to potentially qualify for a teleport multiple times. We could
             * be wrong and they hadn't actually teleported (such as the scenario a player is standing still, teleports
             * to themself, they have a network lag spike after confirming the pre, and then they start moving. Once
             * their network resumes they would teleport but we would have already wrongly assumed they had teleported).
             * This could allow for potential exploitation of the anticheat, but it can be fixed by lag compensating
             * more. The more things we have lag compensated, the more harmful it is for a player to modify this
             * behavior.
             * This could also be fixed with a prediction system, but that's a long ways out.
             */
            if (!teleport.isHandled()) break;
        }
    }

    /**
     * Handle compensating different types of teleport packets.
     */
    @Override
    public void handlePacketPlaySend(PacketPlaySendEvent event) {
        switch (event.getPacketType()) {
            case ENTITY_TELEPORT -> {
                WrapperPlayServerEntityTeleport wrapper = new WrapperPlayServerEntityTeleport(event);
                if (wrapper.getEntityId() != player.getUser().getEntityId()) return;
                Teleport teleport = new Teleport(wrapper.getPosition(), new Pair<>(wrapper.getPitch(), wrapper.getYaw()), wrapper.getRelativeFlags(), wrapper.isOnGround());
                handleTeleport(teleport);
            }
            case PLAYER_ROTATION -> {
                WrapperPlayServerPlayerRotation wrapper = new WrapperPlayServerPlayerRotation(event);
                Teleport teleport = new Teleport(null, new Pair<>(wrapper.getPitch(), wrapper.getYaw()), null, null);
                handleTeleport(teleport);
            }
            case PLAYER_POSITION_AND_LOOK -> {
                WrapperPlayServerPlayerPositionAndLook wrapper = new WrapperPlayServerPlayerPositionAndLook(event);
                Teleport teleport = new Teleport(wrapper.getPosition(), new Pair<>(wrapper.getPitch(), wrapper.getYaw()), wrapper.getRelativeFlags(), null);
                handleTeleport(teleport);
            }
        }
    }

    /**
     * Handle compensating teleports. When the initial confirmation returns, we start including the teleport. When the
     * post confirmation returns, we remove the teleport.
     */
    private void handleTeleport(Teleport teleport) {
        var confirm = player.getConfirmationTracker().confirm();
        confirm.onBegin(() -> {
            eligibleTeleports.add(teleport);
        });
        confirm.onAfterConfirm(() -> {
            remove.add(teleport);
        });
    }

    /**
     * This method checks whether the given Teleport matches the player's current Flying packet. To check this, we see
     * if the onGround status, position, and rotation matches. We also account for relative movements,
     */
    private boolean match(Teleport teleport, WrapperPlayClientPlayerFlying wrapper) {
        /*
         * Player Rotations and Player Position and Look packets don't define a ground status.
         * Entity Teleport define a ground status.
         */
        if (teleport.getOnGround() != null) if (!teleport.getOnGround().equals(wrapper.isOnGround())) return false;


        /*
         * Entity Teleport and Player Position and Look define a position.
         * Player Rotations don't define a position.
         */
        if (teleport.getPosition() != null) {
            if (!wrapper.hasPositionChanged()) return false;
            double x = teleport.getPosition().getX(), y = teleport.getPosition().getY(), z = teleport.getPosition().getZ();

            if (teleport.getRelativeFlags() != null) {
                if (teleport.getRelativeFlags().has(RelativeFlag.X)) x += player.getPositionTracker().getLastX();
                if (teleport.getRelativeFlags().has(RelativeFlag.Y)) y += player.getPositionTracker().getLastY();
                if (teleport.getRelativeFlags().has(RelativeFlag.Z)) z += player.getPositionTracker().getLastZ();
            }

            if (x != wrapper.getLocation().getX() || y != wrapper.getLocation().getY() || z != wrapper.getLocation().getZ()) return false;

        }

        /*
         * Currently no teleport packets don't include a rotation, but I don't trust Mojang so we futureproof!
         */
        if (teleport.getRotation() != null) {
            if (!wrapper.hasRotationChanged()) return false;
            float pitch = teleport.getRotation().getX(), yaw = teleport.getRotation().getY();

            if (teleport.getRelativeFlags() != null) {
                if (teleport.getRelativeFlags().has(RelativeFlag.PITCH)) {
                    // Pitch is bounded -90 to 90
                    if (pitch < 0) {
                        pitch = Math.max(-90, player.getRotationTracker().getLastPitch() + pitch);
                    } else {
                        pitch = Math.min(90, player.getRotationTracker().getLastPitch() + pitch);
                    }
                }
                if (teleport.getRelativeFlags().has(RelativeFlag.YAW)) yaw += player.getRotationTracker().getLastYaw();
            }

            // Verify equality.
            if (pitch != wrapper.getLocation().getPitch() || yaw != wrapper.getLocation().getYaw()) return false;
        }

        return true;
    }
}
