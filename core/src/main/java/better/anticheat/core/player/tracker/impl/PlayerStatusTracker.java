package better.anticheat.core.player.tracker.impl;

import better.anticheat.core.player.Player;
import better.anticheat.core.player.tracker.Tracker;
import better.anticheat.core.player.tracker.impl.confirmation.ConfirmationTracker;
import better.anticheat.core.util.type.xstate.manystate.BooleanManyState;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClientStatus;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateHealth;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Getter
@Slf4j
public class PlayerStatusTracker extends Tracker {
    private final BooleanManyState isDead = new BooleanManyState(10);
    private final ConfirmationTracker confirmationTracker;

    public PlayerStatusTracker(final Player player, final ConfirmationTracker confirmationTracker) {
        super(player);
        this.confirmationTracker = confirmationTracker;
    }

    /*
     * Packet handling.
     */

    @Override
    public void handlePacketPlaySend(final PacketPlaySendEvent event) {
        switch (event.getPacketType()) {
            case PacketType.Play.Server.UPDATE_HEALTH -> {
                final var wrapper = new WrapperPlayServerUpdateHealth(event);

                confirmationTracker
                        .confirm()
                        .onBegin(() -> {
                            isDead.addNew(wrapper.getHealth() == 0);
                            log.info("added to {} due to health", wrapper.getHealth() == 0);
                        })
                        .onAfterConfirm(() -> {
                            isDead.flushOld();
                            log.info("flushed to {} due to health", wrapper.getHealth() == 0);
                        });
            }

            case DEATH_COMBAT_EVENT -> confirmationTracker
                    .confirm()
                    .onBegin(() -> {
                        isDead.addNew(true);
                        log.info("added to true due to death");
                    })
                    .onAfterConfirm(() -> {
                        isDead.flushOld();
                        log.info("flushed to true due to death");
                    });
        }
    }

    public void handlePacketPlayReceive(final PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case CLIENT_STATUS -> {
                final var wrapper = new WrapperPlayClientClientStatus(event);

                if (Objects.requireNonNull(wrapper.getAction()) == WrapperPlayClientClientStatus.Action.PERFORM_RESPAWN) {
                    isDead.addNew(false);
                    // Once client says alive, flush ALL the deads, as client state is all that matters.
                    // This is done to prevent disabler exploits.
                    for (int i = 0; i < 4; i++) {
                        isDead.flushOld();
                    }
                    log.info("added and flushed to false due to respawn");
                }
            }
        }
    }
}
