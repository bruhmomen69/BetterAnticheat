package better.anticheat.sponge;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.player.PlayerManager;
import com.github.retrooper.packetevents.PacketEvents;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DamageEntityEvent;

import java.util.Random;

/**
 * Handles ML-based combat damage modification for Sponge.
 * Monitors player damage events and applies hit cancellation based on CMLTracker averages.
 * Note: Damage modification is not supported on Sponge due to API complexity.
 */
public class CombatDamageListener {

    private final Random random = new Random();

    /**
     * Handles entity damage events to apply ML-based combat modifications.
     * Calculates overall average from all MLCheck history arrays and applies
     * hit cancellation based on configuration.
     *
     * @param event the damage event
     */
    @Listener
    public void onDamageEntity(DamageEntityEvent event) {
        final var damageSource = event.cause().root();
        if (!(damageSource instanceof Player spongePlayer)) return;

        final var user = PacketEvents.getAPI().getPlayerManager().getUser(spongePlayer.uniqueId());
        if (user == null) return;
        
        final var player = PlayerManager.getPlayer(user);
        if (player == null) return;
        
        final var cmlTracker = player.getCmlTracker();
        if (cmlTracker == null) return;
        
        if (!BetterAnticheat.getInstance().isMlCombatDamageEnabled()) return;
        
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
        
        if (totalCount == 0) return;
        
        final double overallAverage = totalSum / totalCount;
        final double threshold = BetterAnticheat.getInstance().getMlCombatDamageThreshold();
        if (overallAverage <= threshold) return;
        
        final double cancellationChance = overallAverage * BetterAnticheat.getInstance().getMlCombatDamageCancellationMultiplier();
        
        if (random.nextDouble() * 100.0 < cancellationChance) {
            event.setCancelled(true);
            return;
        }
        
        // Damage modification not supported on Sponge due to API complexity
    }
}