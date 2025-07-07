package better.anticheat.velocity.quantifier;

import better.anticheat.core.player.PlayerManager;
import com.github.retrooper.packetevents.protocol.player.User;
import org.geysermc.floodgate.api.FloodgateApi;

public class FloodgateQuantifier implements PlayerManager.Quantifier {
    @Override
    public boolean check(User user) {
        final var floodgateApi = FloodgateApi.getInstance();
        if (floodgateApi == null) return true;
        return !floodgateApi.isFloodgatePlayer(user.getUUID());
    }
}
