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

        final var mitigationTracker = playerData.getMitigationTracker();
        if (mitigationTracker == null
                || mitigationTracker.getMitigationTicks().get() <= 0
                || !this.betterAnticheat.isMitigationCombatTickEnabled()) {
            return;
        }

        final boolean generalCheckFailed = (Math.random() * 100) > this.betterAnticheat.getMitigationCombatDamageCancellationChance();

        if (generalCheckFailed) {
            event.setCancelled(true);
        }
    }
}
