package better.anticheat.core.player;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.DataBridge;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerManager {

    private static final Map<User, Player> USER_MAP = new HashMap<>();
    private static final List<Quantifier> QUANTIFIERS = new ArrayList<>();

    /*
     * Quantifier code.
     * A quantifier allows for platform implementations to check certain things about the user before adding them to the
     * anticheat. As of writing this, this is being implemented to allow Bedrock players (via geyser) to be exempted.
     * For a user to be added to the anticheat, every Quantifier must return true.
     */

    public interface Quantifier {
        boolean check(User user);
    }

    public static void registerQuantifier(Quantifier quantifier) {
        QUANTIFIERS.add(quantifier);
    }

    /*
     * Player management.
     */

    public static void addUser(User user, DataBridge dataBridge) {
        for (Quantifier quantifier : QUANTIFIERS) if (!quantifier.check(user)) return;
        USER_MAP.put(user, new Player(user, dataBridge));
    }

    public static void removeUser(User user) throws IOException {
        final var removedPlayer = USER_MAP.remove(user);
        if (removedPlayer == null) return;
        removedPlayer.close();
    }

    public static Player getPlayer(User user) {
        return USER_MAP.get(user);
    }

    public static void load(BetterAnticheat plugin) {
        for (Player player : USER_MAP.values()) player.load();
        plugin.getDataBridge().logInfo("Loaded checks for " + USER_MAP.size() + " players.");
    }

    public static void sendAlert(Component text) {
        /*
         * We use the PacketEvents user collection as to include players who may be excluded via Quantifiers. Just
         * because a player may be logged in via something like Geyser doesn't have anything to do with whether they're
         * a staff member.
         */
        for (User user : PacketEvents.getAPI().getProtocolManager().getUsers()) {
            if (!BetterAnticheat.getInstance().getDataBridge().hasPermission(user, BetterAnticheat.getInstance().getAlertPermission())) continue;
            user.sendMessage(text);
        }
    }
}
