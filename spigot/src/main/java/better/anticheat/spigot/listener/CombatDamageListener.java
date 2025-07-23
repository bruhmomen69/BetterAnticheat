package better.anticheat.spigot.listener;

import better.anticheat.core.BetterAnticheat;
import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Random;

/**
 * Handles ML-based combat damage modification for Spigot.
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
    public void mitigateReduceDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player bukkitPlayer)) return;

        final var user = PacketEvents.getAPI().getPlayerManager().getUser(bukkitPlayer);
        if (user == null) return;

        final var player = BetterAnticheat.getInstance().getPlayerManager().getPlayer(user);
        if (player == null) return;

        final var cmlTracker = player.getCmlTracker();
        if (cmlTracker == null) return;

        if (!BetterAnticheat.getInstance().isMlCombatDamageEnabled()) return;

        if (cmlTracker.getMitigationTicks().get() <= 0) return;

        final double cancellationChance = cmlTracker.getAverageScore() * BetterAnticheat.getInstance().getMlCombatDamageCancellationMultiplier();

        if (random.nextDouble() * 100.0 < cancellationChance) {
            event.setCancelled(true);
            return;
        }

        final double damageMultiplier = BetterAnticheat.getInstance().getMlCombatDamageReductionMultiplier();
        final double damagePercentage = Math.max(0.0, (100.0 - (cmlTracker.getAverageScore() * damageMultiplier)) / 100.0);

        final double originalDamage = event.getDamage();
        final double newDamage = originalDamage * damagePercentage;
        event.setDamage(newDamage);
    }

    /**
     * Increases the damage you take if you are cheating.
     *
     * @param event the damage event
     */
    @EventHandler
    public void mitigateIncreaseDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player bukkitPlayer)) return;

        final var user = PacketEvents.getAPI().getPlayerManager().getUser(bukkitPlayer);
        if (user == null) return;

        final var player = BetterAnticheat.getInstance().getPlayerManager().getPlayer(user);
        if (player == null) return;

        final var cmlTracker = player.getCmlTracker();
        if (cmlTracker == null) return;

        if (!BetterAnticheat.getInstance().isMlCombatDamageEnabled()) return;

        if (cmlTracker.getMitigationTicks().get() <= 0) return;

        final double damageMultiplier = BetterAnticheat.getInstance().getMlCombatDamageReductionMultiplier();
        final double damagePercentage = 1 + Math.max(0.0, (100.0 - (cmlTracker.getAverageScore() * damageMultiplier)) / 100.0);

        final double originalDamage = event.getDamage();
        final double newDamage = originalDamage * damagePercentage;
        event.setDamage(newDamage);
    }
}