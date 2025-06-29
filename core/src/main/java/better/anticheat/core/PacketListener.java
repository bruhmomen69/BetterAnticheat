package better.anticheat.core;

import better.anticheat.core.player.Player;
import better.anticheat.core.player.PlayerManager;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.UserDisconnectEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class PacketListener extends SimplePacketListenerAbstract {
    private final DataBridge dataBridge;

    @Override
    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
        Player player = PlayerManager.getPlayer(event.getUser());
        if (player == null) return;
        player.handleReceivePacket(event);
    }

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.JOIN_GAME) {
            PlayerManager.addUser(event.getUser(), dataBridge);
            return;
        }

        Player player = PlayerManager.getPlayer(event.getUser());
        if (player == null) return;
        player.handleSendPacket(event);
    }

    @Override
    public void onUserDisconnect(UserDisconnectEvent event) {
        try {
            PlayerManager.removeUser(event.getUser());
        } catch (final Exception e) {
            log.error("Error while removing player: ", e);
        }
    }
}
