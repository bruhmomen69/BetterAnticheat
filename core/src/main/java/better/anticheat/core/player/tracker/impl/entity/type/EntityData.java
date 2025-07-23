package better.anticheat.core.player.tracker.impl.entity.type;

import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import wtf.spare.sparej.fastlist.FastObjectArrayList;
import wtf.spare.sparej.incrementer.IntIncrementer;
import wtf.spare.sparej.xstate.bistate.DoubleBiState;
import wtf.spare.sparej.xstate.manystate.ObjectManyState;

import java.util.EnumMap;
import java.util.List;

@Data
public class EntityData {
    public final int id;
    private final EntityType type;
    private final EnumMap<EntityAttribute, Object> attributes = new EnumMap<>(EntityAttribute.class);
    private DoubleBiState serverPosX;
    private DoubleBiState serverPosY;
    private DoubleBiState serverPosZ;
    private ObjectManyState<EntityPose> poses = new ObjectManyState<>(100);
    private float height;
    private float width;
    private EntityTrackerState rootState;
    private IntIncrementer treeSize = new IntIncrementer(1);

    public int compareTo(@NotNull EntityData o) {
        return Integer.compare(o.getId(), this.id);
    }

    @NotNull
    public List<EntityTrackerState> walk() {
        return walk(rootState, new FastObjectArrayList<>(Math.max(0, treeSize.get())));
    }

    @NotNull
    public List<EntityTrackerState> walk(final EntityTrackerState parent, final List<EntityTrackerState> existingEntries) {
        for (final var child : parent.getChildren()) {
            walk(child, existingEntries);
        }
        existingEntries.add(parent);
        return existingEntries;
    }
}
