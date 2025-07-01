package better.anticheat.core.player.tracker.impl.ml;

import better.anticheat.core.player.Player;
import better.anticheat.core.player.tracker.Tracker;
import better.anticheat.core.util.EntityMath;
import better.anticheat.core.util.type.fastlist.ord.OrderedArrayDoubleEvictingList;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;

@Getter
@Setter
public class CMLTracker extends Tracker {
    public CMLTracker(Player player) {
        super(player);
    }

    private final ArrayList<double[][]> recording = new ArrayList<>();
    private final OrderedArrayDoubleEvictingList previousYaws = new OrderedArrayDoubleEvictingList(5);
    private final OrderedArrayDoubleEvictingList previousYawOffsets = new OrderedArrayDoubleEvictingList(5);
    private final OrderedArrayDoubleEvictingList previousEnhancedYawOffsets = new OrderedArrayDoubleEvictingList(5);
    private int lastEntityId;
    private boolean recordingNow = false;

    @Override
    public void handlePacketPlayReceive(final PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case INTERACT_ENTITY -> {
                final var wrapper = new WrapperPlayClientInteractEntity(event);

                this.lastEntityId = wrapper.getEntityId();

                if (!recordingNow) return;

                this.recording.add(new double[][]{
                        Arrays.copyOf(previousYaws.getArray(), previousYaws.getArray().length),
                        Arrays.copyOf(previousYawOffsets.getArray(), previousYawOffsets.getArray().length),
                        Arrays.copyOf(previousEnhancedYawOffsets.getArray(), previousEnhancedYawOffsets.getArray().length)
                });
            }

            case PLAYER_FLYING, PLAYER_POSITION, PLAYER_ROTATION, PLAYER_POSITION_AND_ROTATION -> {
                final var targetTracker = getPlayer().getEntityTracker();
                final var target = targetTracker.getEntities().get(lastEntityId);

                if (target == null|| target.getHeight() < 1.5f || !recordingNow) {
                    return;
                }

                final var targetBox = target.getRootState().getBb();

                final var targetCentre = new Vector3d(targetBox.posX(), targetBox.posY(), targetBox.posZ());

                final var position = getPlayer().getPositionTracker();
                final var rots = getPlayer().getRotationTracker();
                final var player = new Vector3d(position.getX(), position.getY(), position.getZ());
                final double[] offsets = EntityMath.getOffsetFromLocation(player, targetCentre, rots.getYaw(), rots.getPitch());

                this.previousYawOffsets.push(offsets[0]);
                this.previousYaws.push(rots.getDeltaYaw());
                this.previousEnhancedYawOffsets.push(offsets[0] - rots.getDeltaYaw());
            }
        }
    }

    @Override
    public void handlePacketPlaySend(final PacketPlaySendEvent event) {

    }
}
