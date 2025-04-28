package better.anticheat.core.user;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.UserDisconnectEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

import java.util.List;

public class PacketListener extends SimplePacketListenerAbstract {

    @Override
    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
        List<Check> checks = UserManager.getUserChecks(event.getUser());
        if (checks == null) return;
        for (Check check : checks) check.handleReceivePlayPacket(event);
    }

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.JOIN_GAME) {
            UserManager.addUser(event.getUser());
            return;
        }

        List<Check> checks = UserManager.getUserChecks(event.getUser());
        if (checks == null) return;
        for (Check check : checks) check.handleSendPlayPacket(event);
    }

    @Override
    public void onUserDisconnect(UserDisconnectEvent event) {
        UserManager.removeUser(event.getUser());
    }
}
