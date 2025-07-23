package better.anticheat.core.player.tracker.impl.mitigation;

import better.anticheat.core.player.Player;
import better.anticheat.core.player.tracker.Tracker;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAnimation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import dev.hypera.chameleon.event.EventSubscriber;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import wtf.spare.kbsync.api.data.entity.IEntityData;
import wtf.spare.kbsync.api.data.entity.IEntityTrackerData;
import wtf.spare.kbsync.api.events.impl.SyncPacketPlayReceiveEvent;
import wtf.spare.kbsync.api.util.entityraycast.RayCastUtil;
import wtf.spare.kbsync.impl.bukkit.config.HitregConfig;
import wtf.spare.sparej.incrementer.IntIncrementer;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Optional;

// Keep track of received packets that haven't been processed yet, and packets to cancel. Only cancel if received is zero.
// On arm swing, check raycast and attack.
// On use entity, if Math.max(unprocessedPackets - 1, 0) > 0, and cancel queue size >> 0 cancel.
public class HitregMitigationTracker extends Tracker {
    private final IntIncrementer unprocessedFakeCounter = new IntIncrementer(0);
    private final IntIncrementer hitCancelCounter = new IntIncrementer(0);

    public HitregMitigationTracker(Player player) {
        super(player);
    }

    @Override
    public void handlePacketPlayReceive(@NotNull final PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case INTERACT_ENTITY -> {
                final WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
                // Skip non attack packets
                if (packet.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;

                // Skip if unprocessed fake packet
                // Cancel if cancel counter requires it.
                if (this.unprocessedFakeCounter.get() <= 0) {
                    if (this.hitCancelCounter.get() > 0) {
                        this.hitCancelCounter.decrementOrMin(0);
                        event.setCancelled(true);
                    }
                }

                // Decrement unprocessed fake packet counter
                this.unprocessedFakeCounter.decrementOrMin(0);
            }
            case ANIMATION -> {

                // Anti lag
                if (event.getData().getContainer().getTicks().getTicksSinceAttack().get() > this.hitregConfig.getDynamicHitreg().getAntiLagDisableTime() * 20) {
                    return;
                }

                // Anti dig problem
                if (event.getData().getContainer().getTicks().getTicksSinceDigging().get() < 10) {
                    return;
                }

                // Anti place problem
                if (event.getData().getContainer().getTicks().getTicksSincePlace().get() < 5) {
                    return;
                }

                // Swing logic
                final WrapperPlayClientAnimation packet = (WrapperPlayClientAnimation) event.getWrappers().get(WrapperPlayClientAnimation.class);
                if (packet.getHand() == InteractionHand.OFF_HAND) return;

                // Get this, we will be using it a ton.
                final var data = event.getData().getContainer();
                final IEntityTrackerData entityTrackerData = data.getEntity();
                final Location playerPos = data.getPosition().getCurrentLocation();

                for (IEntityData entity : entityTrackerData.getEntities().values()) {
                    // Check for entities where all axises are less than 6 blocks from the player.
                    if (Math.abs(entity.getServerPosX().getCurrent() - playerPos.getX()) < 6 && Math.abs(entity.getServerPosY().getCurrent() - playerPos.getY()) < 6 && Math.abs(entity.getServerPosZ().getCurrent() - playerPos.getZ()) < 6) {
                        // Raycast the hit

                        // Exempt falling blocks and tnt
                        if (entity.getType() == EntityTypes.FALLING_BLOCK || entity.getType() == EntityTypes.TNT || entity.getType() == EntityTypes.PRIMED_TNT || entity.getType() == EntityTypes.DRAGON_FIREBALL) {
                            continue;
                        }

                        // Get the possible yaws
                        final var yaws = new double[]{
                                data.getRotation().getYaw(),
                                data.getRotation().getLastYaw()
                        };

                        // Get the possible pitches
                        final var pitches = new double[]{
                                data.getRotation().getPitch(),
                                data.getRotation().getLastPitch()
                        };

                        // Get the possible locations
                        final var positions = Arrays.asList(
                                data.getPosition().getCurrentLocation(),
                                ((ArrayDeque<Location>) data.getPosition().getLastPositions()).getLast()
                        );


                        // 0.005 is movement offset.
                        // 0.1 is the hitbox cheat I want to give people.
                        var marginOfError = 0.005 + 0.1 + hitregConfig.getReachModifier();

                        if (event.getData().getVersion().isNewerThan(ClientVersion.V_1_9) || !data.getPosition().isPosition()) {
                            marginOfError += data.getAttributes().getEntityReach().getCurrent() / 100.0;
                        }

                        final var rayCastResult = RayCastUtil.getResult(entity, yaws, pitches, positions, marginOfError, 0.1);

                        // Inside entity
                        if (rayCastResult.isCollided()) continue;

                        // Combo timers
                        if (!(hitregConfig.getDynamicHitreg().isTrade()
                                || data.getTicks().getTicksSinceAttack().get() < hitregConfig.getDynamicHitreg().getComboTimer()
                                || data.getTicks().getTicksSinceAttack().get() > (hitregConfig.getDynamicHitreg().getComboTimer() * hitregConfig.getDynamicHitreg().getComboBreaker()))) {
                            continue;
                        }

                        // Check hit stuff
                        if (!(rayCastResult.getDistance() > 0 && rayCastResult.getDistance() < (data.getAttributes().getEntityReach().getCurrent() + 0.1 + hitregConfig.getReachModifier())))
                            continue;

                        // Valid hit detected
                        // Increment the counters that are used on hit
                        data.getKbSync().getHitUnprocessedFakeCounter().increment();
                        data.getKbSync().getHitCancelCounter().increment();

                        // Send packet
                        final Player bPlayer = Bukkit.getPlayer(event.getData().getPlayer().getUUID());
                        if (bPlayer == null) return;
                        if (entity.getType() == EntityTypes.END_CRYSTAL && hitregConfig.isFastCrystals() && PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_20)) {
                            if (rayCastResult.getDistance() < 0.4) continue;
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    for (Entity nearbyEntity : bPlayer.getNearbyEntities(10, 10, 10)) {
                                        if (nearbyEntity.getEntityId() == entity.getId()) {
                                            bPlayer.attack(nearbyEntity);
                                        }
                                    }
                                }
                            }.runTask(javaPlugin);
                        } else {
                            PacketEvents.getAPI().getPlayerManager().receivePacket(bPlayer, new WrapperPlayClientInteractEntity(
                                    entity.getId(),
                                    WrapperPlayClientInteractEntity.InteractAction.ATTACK,
                                    packet.getHand(),
                                    Optional.of(new Vector3f((float) rayCastResult.getVector().getX(), (float) rayCastResult.getVector().getY(), (float) rayCastResult.getVector().getZ())),
                                    Optional.of(data.getAction().isSneaking())));
                        }
                        return;
                    }
                }
            }
            case CLIENT_TICK_END -> hitCancelCounter.set(0);
        }
        // Attack Logic
        if (event.getWrappers().containsKey(WrapperPlayClientInteractEntity.class)) {

        } else if (event.getWrappers().containsKey(WrapperPlayClientAnimation.class)) {
        } else if (event.getWrappers().containsKey(WrapperPlayServerUpdateAttributes.class)) {
            // Reach modifiers, not related to anything else in this class.
            final WrapperPlayServerUpdateAttributes packet = (WrapperPlayServerUpdateAttributes) event.getWrappers().get(WrapperPlayServerUpdateAttributes.class);
            for (WrapperPlayServerUpdateAttributes.Property property : packet.getProperties()) {
                if (property.getAttribute() == Attributes.PLAYER_ENTITY_INTERACTION_RANGE) {
                    if (property.getValue() == 3.0) {
                        property.setValue(3.0 + hitregConfig.getReachModifier());
                    }
                }
            }
        }
    }
}
