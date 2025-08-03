package better.anticheat.paper.util;

import ac.grim.grimac.api.GrimAbstractAPI;
import better.anticheat.core.player.Player;
import better.anticheat.core.player.tracker.impl.confirmation.ConfirmationState;
import better.anticheat.core.player.tracker.impl.confirmation.ConfirmationType;
import better.anticheat.core.player.tracker.impl.confirmation.PlatformNativeConfirmationHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Allows us to hook into Grim Anticheat to reduce the amount of packets we send ourselves by using its packets.
 */
public class GrimHook implements PlatformNativeConfirmationHandler {
    public static final Logger logger = LoggerFactory.getLogger("BetterAnticheat: GrimHook");

    private final JavaPlugin plugin;
    private Object grimInstance = null;

    public GrimHook(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public ConfirmationState confirmPost(Player player, Consumer<ConfirmationState> runnable) {
        if (!(grimInstance instanceof GrimAbstractAPI)) {
            final var grim = plugin.getServer().getServicesManager().getRegistration(GrimAbstractAPI.class);

            if (grim != null) {
                grimInstance = grim.getProvider();
            } else {
                return null;
            }
        }

        final var localGrimInstance = (GrimAbstractAPI) grimInstance;

        final var entry = localGrimInstance.getGrimUser(player.getUser().getUUID());

        if (entry == null) return null;

        final var confirmationState = new ConfirmationState(System.nanoTime(), ConfirmationType.PINGPONG, System.currentTimeMillis(), false);

        entry.addRealTimeTaskNext(() -> {
            runnable.accept(confirmationState);
            logger.debug("Got a packet from Grim: {}", entry.getLastTransactionReceived());
        });

        return confirmationState;
    }
}
