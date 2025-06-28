package better.anticheat.core.player;

import better.anticheat.core.check.Check;
import better.anticheat.core.check.CheckManager;
import better.anticheat.core.player.tracker.impl.PositionTracker;
import better.anticheat.core.player.tracker.impl.RotationTracker;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.player.User;

import java.util.List;

public class Player {

    private final User user;
    private final PositionTracker positionTracker;
    private final RotationTracker rotationTracker;

    private List<Check> checks = null;


    public Player(User user) {
        this.user = user;
        positionTracker = new PositionTracker(this);
        rotationTracker = new RotationTracker(this);
        load();
    }

    /*
     * Getters.
     */

    public PositionTracker getPositionTracker() {
        return positionTracker;
    }

    public RotationTracker getRotationTracker() {
        return rotationTracker;
    }

    public User getUser() {
        return user;
    }

    /*
     * Handle packets.
     */

    public void handleReceivePacket(PacketPlayReceiveEvent event) {
        positionTracker.handlePacketPlayReceive(event);
        rotationTracker.handlePacketPlayReceive(event);
        for (Check check : checks) {
            if (!check.isEnabled()) continue;
            check.handleReceivePlayPacket(event);
        }
    }

    public void handleSendPacket(PacketPlaySendEvent event) {
        positionTracker.handlePacketPlaySend(event);
        rotationTracker.handlePacketPlaySend(event);
        for (Check check : checks) {
            if (!check.isEnabled()) continue;
            check.handleSendPlayPacket(event);
        }
    }

    /*
     *
     */

    public void load() {
        if (checks == null) CheckManager.getChecks(this);
        else for (Check check : checks) check.load();
    }
}
