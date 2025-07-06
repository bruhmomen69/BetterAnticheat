package better.anticheat.velocity;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.player.PlayerManager;
import better.anticheat.core.player.tracker.impl.ml.CMLTracker;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

import java.util.Arrays;

public final class CombatDamageListener extends PacketListenerAbstract {

    private final BetterAnticheat betterAnticheat;

    public CombatDamageListener(BetterAnticheat betterAnticheat) {
        this.betterAnticheat = betterAnticheat;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent rawEvent) {
        if (!(rawEvent instanceof PacketPlayReceiveEvent event)) return;
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
            if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                final var playerData = PlayerManager.getPlayer(event.getUser());
                if (playerData != null) {
                    final var cmlTracker = playerData.getCmlTracker();
                    if (cmlTracker != null) {
                        if (betterAnticheat.isVelocityTickCheckEnabled()) {
                            double totalSum = 0.0;
                            int totalCount = 0;

                            for (final var mlCheck : cmlTracker.getInternalChecks()) {
                                if (!mlCheck.getHistory().isFull()) continue;

                                final double[] historyArray = mlCheck.getHistory().getArray();
                                for (final double value : historyArray) {
                                    totalSum += value;
                                    totalCount++;
                                }
                            }

                            if (totalCount > 0) {
                                final double overallAverage = totalSum / totalCount;
                                if (cmlTracker.getTicksSinceLastAttack() > betterAnticheat.getMaxTicksSinceLastAttack() && overallAverage > betterAnticheat.getMinAverageForTickCheck()) {
                                    event.setCancelled(true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
