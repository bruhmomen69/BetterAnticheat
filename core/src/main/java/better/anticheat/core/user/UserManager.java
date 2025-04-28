package better.anticheat.core.user;

import better.anticheat.core.check.Check;
import better.anticheat.core.check.CheckManager;
import com.github.retrooper.packetevents.protocol.player.User;

import java.util.*;

public class UserManager {

    private static final Map<User, List<Check>> USER_MAP = new HashMap<>();

    public static void addUser(User user) {
        USER_MAP.put(user, CheckManager.getEnabledChecks(user));
    }

    public static void removeUser(User user) {
        USER_MAP.remove(user);
    }

    public static List<Check> getUserChecks(User user) {
        return USER_MAP.get(user);
    }

    public static void load() {
        Set<User> users = new HashSet<>(USER_MAP.keySet());
        for (User user : users) {
            // Prevent a memory leak that would likely never happen.
            // Ensure users aren't added back if they log out during a laggy reload.
            if (!USER_MAP.containsKey(user)) continue;
            USER_MAP.put(user, CheckManager.getEnabledChecks(user));
        }
    }
}
