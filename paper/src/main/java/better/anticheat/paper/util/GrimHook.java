package better.anticheat.paper.util;

import ac.grim.grimac.GrimAPI;
import better.anticheat.core.player.Player;
import better.anticheat.core.player.tracker.impl.confirmation.ConfirmationState;
import better.anticheat.core.player.tracker.impl.confirmation.ConfirmationType;
import better.anticheat.core.util.EasyLoops;

import java.util.function.Consumer;

/**
 * Allows us to hook into Grim Anticheat to reduce the amount of packets we send ourselves by using its packets.
 */
public class GrimHook {
    public static ConfirmationState transact(Player player, Consumer<ConfirmationState> runnable) {
        final var entry = EasyLoops.findFirst(GrimAPI.INSTANCE.getPlayerDataManager().getEntries(), (p) -> p.getUniqueId().equals(player.getUser().getUUID()));

        if (entry == null) return null;

        final var confirmationState = new ConfirmationState(System.nanoTime(), ConfirmationType.PINGPONG, System.currentTimeMillis(), false);

        entry.latencyUtils.addRealTimeTask(entry.lastTransactionSent.get() + 1, () -> runnable.accept(confirmationState));

        return confirmationState;
    }
}
