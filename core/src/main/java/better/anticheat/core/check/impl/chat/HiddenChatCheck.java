package better.anticheat.core.check.impl.chat;

import better.anticheat.core.check.Check;
import better.anticheat.core.check.CheckInfo;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSettings;

@CheckInfo(name = "HiddenChat", category = "chat", config = "checks")
public class HiddenChatCheck extends Check {

    private boolean canChat = true, initalFlag = false;

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {

        /*
         * You can only chat while your chat is visible. It is impossible to chat with a vanilla client when your chat
         * is Hidden.
         * TODO: Look into potential issues with servers that are offline.
         */

        switch (event.getPacketType()) {
            case CLIENT_SETTINGS:
                try {
                    WrapperPlayClientSettings wrapper = new WrapperPlayClientSettings(event);
                    switch (wrapper.getChatVisibility()) {
                        case FULL:
                        case SYSTEM:
                            canChat = true;
                            break;
                        case HIDDEN:
                            canChat = false;
                            break;
                    }
                } catch (final Exception e) {
                    setEnabled(false);
                    return;
                }
                break;
            case CHAT_MESSAGE:
                if (!canChat) {
                    // Settings aren't always sent to update this until after chats are sent.
                    if (initalFlag) fail();
                    else initalFlag = true;
                }
                break;
        }
    }
}