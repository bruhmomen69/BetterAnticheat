package better.anticheat.core.player.tracker.impl.entity;

import better.anticheat.core.DataBridge;
import better.anticheat.core.player.Player;
import better.anticheat.core.player.tracker.Tracker;
import better.anticheat.core.player.tracker.impl.PositionTracker;
import better.anticheat.core.player.tracker.impl.confirmation.ConfirmationTracker;
import better.anticheat.core.player.tracker.impl.entity.type.EntityData;
import better.anticheat.core.player.tracker.impl.entity.type.EntityTrackerState;
import better.anticheat.core.player.tracker.impl.entity.type.SplitEntityUpdate;
import better.anticheat.core.util.BoundingBoxSize;
import better.anticheat.core.util.MathUtil;
import better.anticheat.core.util.type.bistate.DoubleBiState;
import better.anticheat.core.util.type.entity.AxisAlignedBB;
import better.anticheat.core.util.type.fastlist.FastObjectArrayList;
import better.anticheat.core.util.type.incrementer.LongIncrementer;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class EntityTracker extends Tracker {
    public EntityTracker(final Player player, final ConfirmationTracker confirmationTracker, final PositionTracker positionTracker, final DataBridge bridge) {
        super(player);
        this.confirmationTracker = confirmationTracker;
        this.positionTracker = positionTracker;
        this.bridge = bridge;
    }

    // Persistent data
    @Getter
    private final Int2ObjectMap<EntityData> entities = new Int2ObjectRBTreeMap<>();
    @Getter
    private final ArrayDeque<SplitEntityUpdate> awaitingUpdates = new ArrayDeque<>();

    // Session data
    private final ConfirmationTracker confirmationTracker;
    private final PositionTracker positionTracker;
    private final DataBridge bridge;

    // Temporary buffers to avoid allocating each time
    private final ObjectArrayList<EntityTrackerState> stateBuffer = new ObjectArrayList<>();
    private final FastObjectArrayList<EntityTrackerState> stateBuffer2 = new FastObjectArrayList<>();
    private final Int2ObjectOpenHashMap<EntityTrackerState> treeShakeMap = new Int2ObjectOpenHashMap<>();

    private final LongIncrementer fullSizeTreeShakeTimer = new LongIncrementer();

    private int totalMovesThisTick = 0;
    private boolean tickEndSinceFlying = false;

    @Override
    public void handlePacketPlaySend(final PacketPlaySendEvent event) {
        switch (event.getPacketType()) {
            case SPAWN_ENTITY: {
                final var wrapper = new WrapperPlayServerSpawnEntity(event);
                this.createEntity(wrapper.getEntityId(), wrapper.getPosition(), wrapper.getEntityType());
                break;
            }
            case SPAWN_LIVING_ENTITY: {
                final var wrapper = new WrapperPlayServerSpawnLivingEntity(event);
                this.createEntity(wrapper.getEntityId(), wrapper.getPosition(), wrapper.getEntityType());
                break;
            }
            case SPAWN_PLAYER: {
                final var wrapper = new WrapperPlayServerSpawnPlayer(event);
                this.createEntity(wrapper.getEntityId(), wrapper.getPosition(), EntityTypes.PLAYER);
                break;
            }
            case ENTITY_RELATIVE_MOVE: {
                final var wrapper = new WrapperPlayServerEntityRelativeMove(event);
                this.relMove(wrapper.getEntityId(), wrapper.getDeltaX(), wrapper.getDeltaY(), wrapper.getDeltaZ());
                break;
            }
            case ENTITY_RELATIVE_MOVE_AND_ROTATION: {
                final var wrapper = new WrapperPlayServerEntityRelativeMoveAndRotation(event);
                this.relMove(wrapper.getEntityId(), wrapper.getDeltaX(), wrapper.getDeltaY(), wrapper.getDeltaZ());
                break;
            }
            case ENTITY_TELEPORT: {
                final var wrapper = new WrapperPlayServerEntityTeleport(event);
                this.teleport(wrapper.getEntityId(), wrapper.getPosition().getX(), wrapper.getPosition().getY(), wrapper.getPosition().getZ());
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void handlePacketPlayReceive(final PacketPlayReceiveEvent event) {
        // WrapperPlayClientPlayerFlying is the base class for position, look, and position_look
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            this.tickEndSinceFlying = false;
            this.onLivingUpdate();
        }

        if (event.getPacketType() == PacketType.Play.Client.CLIENT_TICK_END) {
            if (!this.tickEndSinceFlying) {
                this.tickEndSinceFlying = true;
                return;
            }

            this.onLivingUpdate();
        }
    }

    /**
     * Creates an entity
     */
    public void createEntity(final int entityId, final @NotNull Vector3d position, final @NotNull EntityType type) {
        if (this.entities.containsKey(entityId)) {
            bridge.runTaskLater(getPlayer().getUser(), () -> createEntity(entityId, position, type), 4);
            return;
        }

        final var entityData = new EntityData(entityId, type);
        entityData.setWidth(BoundingBoxSize.getWidth(entityData));
        entityData.setHeight(BoundingBoxSize.getHeight(entityData));

        entityData.setServerPosX(new DoubleBiState(position.getX()));
        entityData.setServerPosY(new DoubleBiState(position.getY()));
        entityData.setServerPosZ(new DoubleBiState(position.getZ()));

        final var root = new EntityTrackerState(null, entityData, createEntityBox(entityData.getWidth(), entityData.getHeight(), position),
                position.getZ(), position.getY(), position.getX());

        entityData.setRootState(root);
        this.entities.put(entityId, entityData);
    }

    /**
     * Handle relative moves for an entity
     */
    public void relMove(final int entityId, final double deltaX, final double deltaY, final double deltaZ) {
        var confirmation = confirmationTracker.confirm();
        if (!this.entities.containsKey(entityId)) {
            confirmation.getOnBegin().thenRun(() -> relMove(entityId, deltaX, deltaY, deltaZ));
            return;
        }

        final var entity = this.entities.get(entityId);
        final var newState = new FastObjectArrayList<EntityTrackerState>();

        confirmation.getOnBegin().thenRun(() -> {
            final var originalRoot = entity.getRootState().cloneWithoutChildren();

            entity.getServerPosX().addNew(entity.getServerPosX().getCurrent() + deltaX);
            entity.getServerPosY().addNew(entity.getServerPosY().getCurrent() + deltaY);
            entity.getServerPosZ().addNew(entity.getServerPosZ().getCurrent() + deltaZ);

            stateBuffer.clear();
            stateBuffer2.clear();
            recursivelyRelMovePre(entity.getRootState(), 0);
            newState.addAll(stateBuffer);
            stateBuffer.clear();
            
            this.awaitingUpdates.add(new SplitEntityUpdate(entity, originalRoot, entity.getServerPosX().getCurrent(),
                    entity.getServerPosY().getCurrent(), entity.getServerPosZ().getCurrent()));
        });

        confirmation.getOnAfterConfirm().thenRun(() -> {
            // Update all then shake tree
            entity.getServerPosX().flushOld();
            entity.getServerPosY().flushOld();
            entity.getServerPosZ().flushOld();

            final var theDelta = MathUtil.hypot(deltaX, deltaY, deltaZ);
            var removedCnt = 0;

            for (final var neww : newState) {
                final var dist = neww.distance(neww.getParent());
                if (dist < neww.getPotentialOffsetAmount() || dist <= theDelta) {
                    entity.getTreeSize().decrement(neww.getChildren().size() + 1);
                    neww.getParent().getChildren().removeExact(neww);
                    removedCnt++;
                } else {
                    for (final var child : neww.getChildren()) {
                        setPositionAndRotation2(child, entity.getServerPosX().getCurrent(), entity.getServerPosY().getCurrent(), entity.getServerPosY().getCurrent());
                    }
                    setPositionAndRotation2(neww, entity.getServerPosX().getCurrent(), entity.getServerPosY().getCurrent(), entity.getServerPosY().getCurrent());
                }
            }

            for (final var edata : this.awaitingUpdates.toArray(new SplitEntityUpdate[0])) {
                if (edata.getData().equals(entity)) {
                    removedCnt -= edata.getFlyings().get() == 0 ? 0 : (edata.getFlyings().get() - 1);
                    this.awaitingUpdates.remove(edata);
                }
            }

            if ((newState.size() - removedCnt) > 0) {
                shakeTree(entity);
            }
        });
    }

    /**
     * handles a relmove packet on PRE tranny
     */
    public void recursivelyRelMovePre(final EntityTrackerState state, final int depth) {
        if (stateBuffer2.indexOfExact(state) != -1) return;

        if (depth < 7) {
            // This + depth checking can remove very low chance branches.
            final boolean shouldClone = Math.abs(state.getPosX() - state.getData().getServerPosX().getCurrent()) > 0.005
                    || Math.abs(state.getPosY() - state.getData().getServerPosY().getCurrent()) > 0.005
                    || Math.abs(state.getPosZ() - state.getData().getServerPosZ().getCurrent()) > 0.005;

            if (shouldClone) {
                // Clone first - the children will be cloned themselves. This improves performance a little.
                final var neww = state.newChild(state, false, false);

                // Recursively run
                final var childDepth = depth + 1;
                for (final var child : state.getChildren()) {
                    stateBuffer2.add(child);
                    recursivelyRelMovePre(child, childDepth);
                }

                // Add the clone after doing recursion to avoid accidentally setting the pos on the old entity state.
                state.getData().getTreeSize().increment(1);
                state.getChildren().add(neww);

                // Tick this on post instead.
                this.stateBuffer.add(neww);
            } else {
                final var childDepth = depth + 1;
                for (final var child : state.getChildren()) {
                    stateBuffer2.add(child);
                    recursivelyRelMovePre(child, childDepth);
                }
            }
        } else {
            // Do not add to state buf 2
            final var childDepth = depth + 1;
            for (final var child : state.getChildren()) {
                recursivelyRelMovePre(child, childDepth);
            }
        }

        // Set the data
        setPositionAndRotation2(state, state.getData().getServerPosX().getCurrent(), state.getData().getServerPosY().getCurrent(), state.getData().getServerPosZ().getCurrent());
    }

    public void teleport(final int entityId, final double x, final double y, final double z) {
        var confirmation = this.confirmationTracker.confirm();
        if (!this.entities.containsKey(entityId)) {
            confirmation.onBegin((a) -> teleport(entityId, x, y, z));
            return;
        }

        final var entity = this.entities.get(entityId);
        final var newState = new FastObjectArrayList<EntityTrackerState>();

        confirmation.onBegin((a) -> {
            entity.getServerPosX().addNew(x);
            entity.getServerPosY().addNew(y);
            entity.getServerPosZ().addNew(z);

            stateBuffer.clear();
            recursivelyTeleportPre(entity.getRootState(), 0);
            newState.addAll(stateBuffer);
            stateBuffer.clear();
        });

        confirmation.onAfterConfirm((a) -> {
            // Update all then shake tree
            entity.getServerPosX().flushOld();
            entity.getServerPosY().flushOld();
            entity.getServerPosZ().flushOld();

            for (final var neww : newState) {
                for (final var child : neww.getChildren()) {
                    setPositionAndRotation2(child, entity.getServerPosX().getCurrent(), entity.getServerPosY().getCurrent(), entity.getServerPosZ().getCurrent());
                }
                setPositionAndRotation2(neww, entity.getServerPosX().getCurrent(), entity.getServerPosY().getCurrent(), entity.getServerPosZ().getCurrent());
            }

            // We just did a tp, we can prune some old branches. This will improve performance.
            treeShrinkRecursive(entity.getRootState(), 0, 4);

            // Do a basic tree shake to remove duplicates
            shakeTree(entity);
        });
    }

    /**
     * Living update
     */
    public void onLivingUpdate() {
        // Handle splits
        for (final var awaitingUpdate : this.awaitingUpdates) {
            // Max of 3 updates.
            if (awaitingUpdate.getFlyings().increment() > 1 && awaitingUpdate.getFlyings().get() <= 4) {
                final var newUpdate = awaitingUpdate.getOldState().newChild(awaitingUpdate.getData().getRootState(), false);
                setPositionAndRotation2(newUpdate, awaitingUpdate.getX(), awaitingUpdate.getY(), awaitingUpdate.getZ());

                // Add the new child.
                awaitingUpdate.getData().getTreeSize().increment(newUpdate.getChildren().size() + 1);
                awaitingUpdate.getData().getRootState().getChildren().add(newUpdate);
            }
        }

        // Tick the entities
        for (final var value : this.entities.values()) {
            this.totalMovesThisTick = 0;
            final var cnt = onLivingUpdateRecursive(value.getRootState());

            // Tree shake if needed
            if ((float) cnt > (value.getTreeSize().get() / 4.0F)) {
                shakeTree(value);
                totalMovesThisTick += cnt;
            }
        }
    }

    /**
     * Shake a tree
     *
     * @param entityData
     */
    public synchronized void shakeTree(final @NotNull EntityData entityData) {
        try {
            if (entityData.getTreeSize().get() > 30) {
                fullSizeTreeShakeTimer.increment();
            }

            if (fullSizeTreeShakeTimer.get() % 50.0 == 0.0) {
                // Get rid of not very useful data, and do emergency cleanup if >> 200
                final var maxDelta = entityData.getTreeSize().get() > 200 ? 0.12 : entityData.getTreeSize().get() > 80 ? 0.03 : entityData.getTreeSize().get() > 65 ? 0.025 : 0.015;

                shakeTreeRecursive(entityData.getRootState(), (state) -> {
                    var statee = (EntityTrackerState) state;
                    var hashCode = statee.liteHashCode();
                    var remove = treeShakeMap.get(hashCode) == statee || (statee.getParent() != null && statee.distance(statee.getParent()) < maxDelta);
                    if (!remove && !treeShakeMap.containsKey(hashCode)) {
                        treeShakeMap.put(hashCode, statee);
                    }

                    return remove;
                });
            } else {
                // Get rid of blatant duplicates
                shakeTreeRecursive(entityData.getRootState(), (state) -> {
                    var statee = (EntityTrackerState) state;
                    var hashCode = statee.hashCodePositionsAndIncrementsOnly();
                    var remove = treeShakeMap.get(hashCode) == statee;
                    if (!remove && !treeShakeMap.containsKey(hashCode)) {
                        treeShakeMap.put(hashCode, statee);
                    }

                    return remove;
                });
            }


            // do not run instantly, wait a little.
            if (positionTracker.getTicks() % 20 == 0.0) {
                // Run a special task on massively oversized trees
                // This is an emergency task when things get bad
                if (entityData.getTreeSize().get() > 120) {
                    treeShrinkRecursive(entityData.getRootState(), 0, 10);
                }
            }
        } finally {
            this.treeShakeMap.clear();
        }
    }

    /**
     * Create the bounding box for an entity
     */
    public @NotNull AxisAlignedBB createEntityBox(final float width, final float height, final @NotNull Vector3d vector3d) {
        return new AxisAlignedBB(vector3d.getX(), vector3d.getY(), vector3d.getZ(), width, height);
    }

    public @NotNull List<EntityData> getEntitiesWithinAABBExcludingEntity(final int entityId, final @NotNull AxisAlignedBB bb) {
        final var entities = new ArrayList<EntityData>();
        for (final var value : this.entities.values()) {
            if (value.getId() == entityId) {
                continue;
            }
            if (value.getRootState().getBb().intersectsWith(bb)) {
                entities.add(value);
            }
        }

        return entities;
    }

    /**
     * handles a teleport packet on PRE tranny
     */
    public void recursivelyTeleportPre(final EntityTrackerState state, final int depth) {
        // Heavy sane depth checking, as we don't need crazy shit for tps.
        if (depth > 4 || (Math.abs(state.getData().getServerPosX().getCurrent() - state.getOtherPlayerMPX()) < 0.005 &&
                Math.abs(state.getData().getServerPosY().getCurrent() - state.getOtherPlayerMPY()) < 0.005 &&
                Math.abs(state.getData().getServerPosZ().getCurrent() - state.getOtherPlayerMPZ()) < 0.005)) {
            setPositionAndRotation2(state, state.getData().getServerPosX().getCurrent(), state.getData().getServerPosY().getCurrent(), state.getData().getServerPosZ().getCurrent());
            for (final var child : state.getChildren()) {
                recursivelyTeleportPre(child, depth + 1);
            }
        } else {
            // Clone first
            final var neww = state.newChild(state, false, false);

            // Recursively run
            for (final var child : state.getChildren()) {
                recursivelyTeleportPre(child, depth + 1);
            }

            // Add the clone after doing recursion to avoid accidentally setting the pos on the old entity state.
            state.getData().getTreeSize().increment(1 + neww.getChildren().size());
            state.getChildren().add(neww);

            // Tick this on post instead.
            this.stateBuffer.add(neww);

            // Set the data
            setPositionAndRotation2(state, state.getData().getServerPosX().getCurrent(), state.getData().getServerPosY().getCurrent(), state.getData().getServerPosZ().getCurrent());
        }
    }

    private void setPositionAndRotation2(final EntityTrackerState state, final double x, final double y, final double z) {
        state.setOtherPlayerMPX(x);
        state.setOtherPlayerMPY(y);
        state.setOtherPlayerMPZ(z);
        state.setOtherPlayerMPPosRotationIncrements(3);
    }

    /**
     * Ticks an entity
     *
     * @return the updates
     */
    public int onLivingUpdateRecursive(final EntityTrackerState state) {
        int cnt = 0;
        if (state.getOtherPlayerMPPosRotationIncrements() > 0) {
            final double d0 = state.getPosX() + (state.getOtherPlayerMPX() - state.getPosX()) / state.getOtherPlayerMPPosRotationIncrements();
            final double d1 = state.getPosY() + (state.getOtherPlayerMPY() - state.getPosY()) / state.getOtherPlayerMPPosRotationIncrements();
            final double d2 = state.getPosZ() + (state.getOtherPlayerMPZ() - state.getPosZ()) / state.getOtherPlayerMPPosRotationIncrements();

            state.setPotentialOffsetAmount(MathUtil.hypot(d0, d1, d2));

            state.setOtherPlayerMPPosRotationIncrements(state.getOtherPlayerMPPosRotationIncrements() - 1);

            // setPosition
            final var lastPosX = state.getPosX();
            final var lastPosY = state.getPosY();
            final var lastPosZ = state.getPosZ();

            state.setPosX(d0);
            state.setPosY(d1);
            state.setPosZ(d2);

            float f = state.getData().getWidth() / 2.0F;
            state.getBb().setMinX(state.getPosX() - f);
            state.getBb().setMinY(state.getPosY());
            state.getBb().setMinZ(state.getPosZ() - f);

            state.getBb().setMinX(state.getPosX() + f);
            state.getBb().setMinY(state.getPosY() + state.getData().getHeight());
            state.getBb().setMinZ(state.getPosZ() + f);

            // Total moves this tick
            if (Math.abs(lastPosX - state.getPosX()) > 0.0005D || Math.abs(lastPosY - state.getPosY()) > 0.0005D || Math.abs(lastPosZ - state.getPosZ()) > 0.0005D) {
                this.totalMovesThisTick++;

                if (state.getOtherPlayerMPPosRotationIncrements() == 0) {
                    cnt++;
                }
            }
        } else {
            state.setPotentialOffsetAmount(0);
        }

        for (final var child : state.getChildren()) {
            cnt += onLivingUpdateRecursive(child);
        }

        return cnt;
    }

    private void treeShrinkRecursive(final EntityTrackerState entityTrackerState, final int depth, final int maxDepth) {
        if (depth >= maxDepth) {
            entityTrackerState.getData().getTreeSize().decrement(entityTrackerState.getChildren().size());
            entityTrackerState.getChildren().clear();
        } else {
            // Also remove children if it's ridiculous
            int childCount = 0;
            stateBuffer2.clear();
            for (final var child : entityTrackerState.getChildren()) {
                if (childCount++ > depth * 5) {
                    stateBuffer2.add(child);
                }
            }

            for (final var iEntityTrackerState : stateBuffer2) {
                entityTrackerState.getChildren().remove(iEntityTrackerState);
            }

            // Tree shrink shit
            final var childDepth = depth + 1;
            for (final var child : entityTrackerState.getChildren()) {
                treeShrinkRecursive(child, childDepth, maxDepth);
            }
        }
    }

    /**
     * Runs the actual logic for tree shaking.
     */
    private void shakeTreeRecursive(final EntityTrackerState entityTrackerState, final Object2BooleanFunction<EntityTrackerState> shouldDelete) {
        stateBuffer.clear(); // Flush old buffers.

        // Remove duplicated entries, and copy their children to the parent (current) node.
        for (final var child : entityTrackerState.getChildren()) {
            if (shouldDelete.getBoolean(child)) {
                // Decrement and remove
                entityTrackerState.getData().getTreeSize().decrement();
                entityTrackerState.getChildren().removeExact(child);

                // Copy the entry to the parent.
                for (final var childChild : child.getChildren()) {
                    stateBuffer.add(childChild);
                }
            }
        }

        // If state buffer is not empty, then add it to children, and call this again until it works.
        if (!stateBuffer.isEmpty()) {
            // Copy and flush the data buffer.
            entityTrackerState.getData().getTreeSize().increment(stateBuffer.size());
            entityTrackerState.getChildren().addAll(stateBuffer);
            stateBuffer.clear();
            // Recurse
            shakeTreeRecursive(entityTrackerState, shouldDelete);
            return; // Skip the following code as recursion will handle it.
        }

        // Tree shake all the children.
        // Use an array to avoid alloc.
        final var childrenArray = entityTrackerState.getChildren().getRawArray();
        final var childrenLen = entityTrackerState.getChildren().size();
        for (int i = 0; i < childrenLen; i++) {
            final var child = childrenArray[i];

            shakeTreeRecursive((EntityTrackerState) child, shouldDelete);
        }
    }

    /**
     * Gets all entities colliding with aabb
     *
     * @param bbThis the aabb to check entities with
     * @param out    the list to add entities to
     * @return the entities
     */
    public List<EntityData> getCollidingEntities(final AxisAlignedBB bbThis, final List<EntityData> out) {
        for (final var edata : this.entities.values()) {
            if (edata.getId() == getPlayer().getUser().getEntityId()) continue;

            final var bb = edata.getRootState().getBb();

            if (bb != null) {
                if (bb.distance(bbThis) <= 4) {
                    out.add(edata);
                }
            }
        }
        return out;
    }
}
