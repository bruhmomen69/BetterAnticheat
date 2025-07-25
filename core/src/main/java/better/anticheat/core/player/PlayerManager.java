package better.anticheat.core.player;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.DataBridge;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.player.User;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import lombok.Getter;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {

    private final BetterAnticheat plugin;

    @Getter
    private final Map<User, Player> userMap = new ConcurrentHashMap<>();
    private final Int2ObjectMap<Player> idMap = new Int2ObjectRBTreeMap<>();
    private final List<Quantifier> quantifiers = new ArrayList<>();

    public PlayerManager(BetterAnticheat plugin) {
        this.plugin = plugin;
    }

    /*
     * Quantifier code.
     * A quantifier allows for platform implementations to check certain things about the user before adding them to the
     * anticheat. As of writing this, this is being implemented to allow Bedrock players (via geyser) to be exempted.
     * For a user to be added to the anticheat, every Quantifier must return true.
     */

    public interface Quantifier {
        boolean check(User user);
    }

    public void registerQuantifier(Quantifier quantifier) {
        quantifiers.add(quantifier);
    }

    /*
     * Player management.
     */

    public synchronized void addUser(User user, DataBridge dataBridge) {
        for (Quantifier quantifier : quantifiers) if (!quantifier.check(user)) return;
        userMap.put(user, new Player(plugin, user, dataBridge));
        idMap.put(user.getEntityId(), userMap.get(user));
    }

    public synchronized void removeUser(User user) throws IOException {
        idMap.remove(user.getEntityId());
        final var removedPlayer = userMap.remove(user);
        if (removedPlayer == null) return;
        removedPlayer.close();
    }

    public Player getPlayer(User user) {
        return userMap.get(user);
    }

    public Player getPlayerByEntityId(int id) {
        return idMap.get(id);
    }

    public Player getPlayerByUsername(String username) {
        for (final var value : userMap.values()) {
            if (value.getUser().getName().equalsIgnoreCase(username)) return value;
        }

        return null;
    }

    public void load() {
        for (Player player : userMap.values()) player.load();
        plugin.getDataBridge().logInfo("Loaded checks for " + userMap.size() + " players.");
    }

    public void sendAlert(Component text) {
        /*
         * We use the PacketEvents user collection as to include players who may be excluded via Quantifiers. Just
         * because a player may be logged in via something like Geyser doesn't have anything to do with whether they're
         * a staff member.
         */
        for (final var user : PacketEvents.getAPI().getProtocolManager().getUsers()) {
            // Do not send alerts to users who are not in the play state
            if (user.getConnectionState() != ConnectionState.PLAY) continue;
            final var player = getPlayer(user);
            if ((player == null && !plugin.getDataBridge().hasPermission(user, BetterAnticheat.getInstance().getAlertPermission()))
                    || (player != null && !player.isAlerts())) continue;
            user.sendMessage(text);
        }
    }

    public void sendVerbose(Component text) {
        // We do not use PacketEvents user collection here as we REQUIRE the player object to process this.
        for (final var player : userMap.values()) {
            if (!player.isVerbose()) continue;
            // Do not send alerts to users who are not in the play state
            if (player.getUser().getConnectionState() != ConnectionState.PLAY) continue;

            player.getUser().sendMessage(text);
        }
    }
}
