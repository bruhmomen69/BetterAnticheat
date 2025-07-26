package better.anticheat.sponge.listener;

import better.anticheat.core.BetterAnticheat;
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
        
        final var player = BetterAnticheat.getInstance().getPlayerManager().getPlayer(user);
        if (player == null) return;
        
        final var mitigationTracker = player.getMitigationTracker();
        if (mitigationTracker == null) return;
        
        if (!BetterAnticheat.getInstance().isMitigationCombatDamageEnabled()) return;

        if (mitigationTracker.getMitigationTicks().get() <= 0) return;
        
        final double cancellationChance = BetterAnticheat.getInstance().getMitigationCombatDamageCancellationChance();
        
        if (random.nextDouble() * 100.0 < cancellationChance) {
            event.setCancelled(true);
            return;
        }
        
        // Damage modification not supported on Sponge due to API complexity
    }
}