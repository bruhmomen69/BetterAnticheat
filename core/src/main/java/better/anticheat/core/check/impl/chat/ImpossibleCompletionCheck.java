package better.anticheat.core.check.impl.chat;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTabComplete;

public class ImpossibleCompletionCheck extends Check {

    public ImpossibleCompletionCheck() {
        super("ImpossibleCompletion");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {

        /*
         * This check verifies that you have attempted to type something before attempting a tab completion.
         * You cannot tab complete an empty string.
         */

        if (event.getPacketType() != PacketType.Play.Client.TAB_COMPLETE) return;
        WrapperPlayClientTabComplete wrapper = new WrapperPlayClientTabComplete(event);
        if (wrapper.getText().isEmpty()) fail();
    }
}
