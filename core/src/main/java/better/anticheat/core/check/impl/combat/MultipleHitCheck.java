package better.anticheat.core.check.impl.combat;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.check.Check;
import better.anticheat.core.check.CheckInfo;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

/**
 * This check looks for hitting multiple entities within a tick.
 */
@CheckInfo(name = "MultipleHit", category = "combat")
public class MultipleHitCheck extends Check {

    private Integer lastEnemy;

    public MultipleHitCheck(BetterAnticheat plugin) {
        super(plugin);
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {

        /*
         * You cannot interact with more than one entity in a tick.
         */

        switch (event.getPacketType()) {
            case CLIENT_TICK_END:
                lastEnemy = null;
                break;
            case INTERACT_ENTITY:
                WrapperPlayClientInteractEntity interactEntity = new WrapperPlayClientInteractEntity(event);

                final int enemy = interactEntity.getEntityId();

                if (lastEnemy != null && lastEnemy != enemy) {
                    fail();
                }

                lastEnemy = enemy;
                break;
        }
    }
}
