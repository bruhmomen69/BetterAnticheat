package better.anticheat.core.check.impl.chat;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;

public class ImpossibleMessageCheck extends Check {

    public ImpossibleMessageCheck() {
        super("ImpossibleMessage");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.CHAT_MESSAGE) return;
        WrapperPlayClientChatMessage wrapper = new WrapperPlayClientChatMessage(event);
        String message = wrapper.getMessage();

        // You cannot send empty messages or messages starting with spaces.
        if (message.trim().isEmpty()) fail("emp");
        else if (message.charAt(0) == ' ') fail("spc");
    }
}
