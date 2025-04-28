package better.anticheat.core.check.impl.combat;

import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

public class MultipleHitCheck extends Check {

    private Integer lastEnemy;

    public MultipleHitCheck() {
        super("MultipleHit");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
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
