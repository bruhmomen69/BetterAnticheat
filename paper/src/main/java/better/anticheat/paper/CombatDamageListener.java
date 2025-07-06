package better.anticheat.paper;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.player.PlayerManager;
import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Random;

/**
 * Handles ML-based combat damage modification for Paper.
 * Monitors player damage events and applies modifications based on CMLTracker averages.
 */
public class CombatDamageListener implements Listener {

    private final Random random = new Random();

    /**
     * Handles entity damage events to apply ML-based combat modifications.
     * Calculates overall average from all MLCheck history arrays and applies
     * hit cancellation or damage reduction based on configuration.
     *
     * @param event the damage event
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        
        final Player bukkitPlayer = (Player) event.getDamager();
        final var user = PacketEvents.getAPI().getPlayerManager().getUser(bukkitPlayer);
        if (user == null) return;
        
        final var player = BetterAnticheat.getInstance().getPlayerManager().getPlayer(user);
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
        
        final double damageMultiplier = BetterAnticheat.getInstance().getMlCombatDamageReductionMultiplier();
        final double damagePercentage = Math.max(0.0, (100.0 - (overallAverage * damageMultiplier)) / 100.0);
        
        final double originalDamage = event.getDamage();
        final double newDamage = originalDamage * damagePercentage;
        event.setDamage(newDamage);
    }
}