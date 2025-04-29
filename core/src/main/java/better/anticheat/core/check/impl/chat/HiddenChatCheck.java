package better.anticheat.core.check.impl.chat;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSettings;

public class HiddenChatCheck extends Check {

    private boolean canChat = true;

    public HiddenChatCheck() {
        super("HiddenChat");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case CLIENT_SETTINGS:
                WrapperPlayClientSettings wrapper = new WrapperPlayClientSettings(event);

                /*
                 * You can only chat while your chat is visible.
                 * It is impossible to chat with a vanilla client when your chat is Hidden.
                 */

                switch (wrapper.getChatVisibility()) {
                    case FULL:
                    case SYSTEM:
                        canChat = true;

                        break;
                    case HIDDEN:
                        canChat = false;

                        break;
                }
                break;
            case CHAT_MESSAGE:
                if (!canChat) fail();
                break;
        }
    }
}
