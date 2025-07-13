package better.anticheat.velocity.listener;

import better.anticheat.core.BetterAnticheat;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

public final class CombatDamageListener extends PacketListenerAbstract {

    private final BetterAnticheat betterAnticheat;

    public CombatDamageListener(BetterAnticheat betterAnticheat) {
        this.betterAnticheat = betterAnticheat;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent rawEvent) {
        if (!(rawEvent instanceof PacketPlayReceiveEvent event)) {
            return;
        }

        if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) {
            return;
        }

        final var wrapper = new WrapperPlayClientInteractEntity(event);
        if (wrapper.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
            return;
        }

        final var playerData = this.betterAnticheat.getPlayerManager().getPlayer(event.getUser());
        if (playerData == null) {
            return;
        }

        final var cmlTracker = playerData.getCmlTracker();
        if (cmlTracker == null || !this.betterAnticheat.isVelocityTickCheckEnabled()) {
            return;
        }

        var totalSum = 0.0;
        var totalCount = 0;

        for (final var mlCheck : cmlTracker.getInternalChecks()) {
            if (!mlCheck.getHistory().isFull()) {
                continue;
            }

            final var historyArray = mlCheck.getHistory().getArray();
            for (final double value : historyArray) {
                totalSum += value;
                totalCount++;
            }
        }

        if (totalCount == 0) {
            return;
        }

        final double overallAverage = totalSum / totalCount;
        final boolean isAttackTooFast = cmlTracker.getTicksSinceLastAttack() < this.betterAnticheat.getMinTicksSinceLastAttack();
        final boolean isAverageTooHigh = overallAverage > this.betterAnticheat.getMinAverageForTickCheck();

        final boolean generalCheckThreshold = overallAverage > this.betterAnticheat.getMlCombatDamageThreshold();
        final boolean generalCheckChance = (Math.random() * 100) > (overallAverage * this.betterAnticheat.getMlCombatDamageCancellationMultiplier());

        final boolean tickCheckFailed = isAttackTooFast && isAverageTooHigh;
        final boolean generalCheckFailed = generalCheckThreshold && generalCheckChance;

        if (tickCheckFailed || generalCheckFailed) {
            event.setCancelled(true);
        }
    }
}
