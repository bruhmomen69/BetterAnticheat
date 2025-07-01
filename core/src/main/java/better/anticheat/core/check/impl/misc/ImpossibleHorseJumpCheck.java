package better.anticheat.core.check.impl.misc;

import better.anticheat.core.check.Check;
import better.anticheat.core.check.CheckInfo;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;

@CheckInfo(name = "ImpossibleHorseJump", category = "misc", config = "checks")
public class ImpossibleHorseJumpCheck extends Check {

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.ENTITY_ACTION) return;
        WrapperPlayClientEntityAction wrapper = new WrapperPlayClientEntityAction(event);

        if (wrapper.getAction() != WrapperPlayClientEntityAction.Action.START_JUMPING_WITH_HORSE) return;
        int boost = wrapper.getJumpBoost();

        /*
         * The horse jump boost mechanic works in an interesting way:
         * First, it starts at 0. Every tick past that it adds 10 to it until it hits 100. After it hits 100, it
         * will decay by a value of 1 per tick until it hits 80, where it will stay. This means that every value
         * under 80 has to be divisible by 10 as it's only that low when rising.
         */
        if (boost < 80 && boost % 10 != 0) fail("<80");
        else if (boost > 100) fail(">100");
    }
}
