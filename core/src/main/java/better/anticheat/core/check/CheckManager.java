package better.anticheat.core.check;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.check.impl.chat.HiddenChatCheck;
import better.anticheat.core.check.impl.chat.ImpossibleCompletionCheck;
import better.anticheat.core.check.impl.chat.ImpossibleMessageCheck;
import better.anticheat.core.check.impl.combat.*;
import better.anticheat.core.check.impl.dig.DigBlockFacePositionCheck;
import better.anticheat.core.check.impl.dig.DigOrderCheck;
import better.anticheat.core.check.impl.dig.MultiBreakCheck;
import better.anticheat.core.check.impl.dig.NoSwingDigCheck;
import better.anticheat.core.check.impl.misc.*;
import better.anticheat.core.check.impl.packet.KeepAliveOrderCheck;
import better.anticheat.core.check.impl.packet.PingPongOrderCheck;
import better.anticheat.core.check.impl.packet.PostCheck;
import better.anticheat.core.check.impl.packet.TeleportConfirmOrderCheck;
import better.anticheat.core.check.impl.place.PlaceBlockFacePositionCheck;
import better.anticheat.core.check.impl.position.*;
import better.anticheat.core.configuration.ConfigSection;
import com.github.retrooper.packetevents.protocol.player.User;

import java.util.*;

public class CheckManager {

    private static final List<Check> CHECKS;

    private CheckManager() {}

    static {
        CHECKS = Arrays.asList(
                // Chat Checks
                new HiddenChatCheck(),
                new ImpossibleCompletionCheck(),
                new ImpossibleMessageCheck(),

                // Combat Checks
                /*
                 * NoSlow and RepeatedRelease were checks in Quality Control that were stable, but it seems like later
                 * versions of Minecraft than what I had designed those checks for originally may have altered how the
                 * Use Item packet works. As of now, these two checks false flag and so I am keeping them out of any
                 * potential releases.
                 */
                new ActionInteractOrderCheck(),
                new DualClickCheck(),
                new InvalidReleaseValuesCheck(),
                new InvalidUseActionsCheck(),
                new MultipleActionCheck(),
                new MultipleHitCheck(),
                //new NoSlowCheck(),
                new NoSwingCombatCheck(),
                //new RepeatedReleaseCheck(),
                new SlotInteractOrderCheck(),

                // Dig Checks
                new DigBlockFacePositionCheck(),
                new DigOrderCheck(),
                new MultiBreakCheck(),
                new NoSwingDigCheck(),

                // Misc Checks
                new ImpossibleHorseJumpCheck(),
                new ImpossibleSlotCheck(),
                new LargeNameCheck(),
                new MultipleSlotCheck(),
                new SmallRenderCheck(),

                // Packet Checks
                new PingPongOrderCheck(),
                new PostCheck(),
                new TeleportConfirmOrderCheck(),

                // Place Checks
                new PlaceBlockFacePositionCheck(),

                // Position Checks
                new FlyingSequenceCheck(),
                new ImpossiblePositionCheck(),
                new ImpossibleRotationCheck(),
                new RepeatedSteerCheck()
        );
    }

    public static Collection<Check> getAllChecks() {
        return Collections.unmodifiableList(CHECKS);
    }

    public static List<Check> getEnabledChecks(User user) {
        /*
         * Do NOT return the existing array list. That would lead to multiple users using the same list, creating
         * concurrency issues.
         * The advantage of this method is also that cloning does not call the constructor, meaning that only the
         * original copies present in the CHECKS list will be reloaded.
         */
        List<Check> returnList = new ArrayList<>();
        for (Check check : CHECKS) {
            if (!check.isEnabled()) continue;
            returnList.add(check.copy(user));
        }

        System.out.println("Loaded " + returnList.size() + " checks for " + user.getName());
        return returnList;
    }

    public static void load(BetterAnticheat plugin) {
        ConfigSection checks = plugin.getFile("checks.yml", BetterAnticheat.class.getResourceAsStream("/checks.yml")).load();
        int enabled = 0;
        for (Check check : CHECKS) {
            check.load(checks.getConfigSection(check.getType().toLowerCase()));
            if (check.isEnabled()) enabled++;
        }
        plugin.getDataBridge().logInfo("Loaded " + CHECKS.size() + " checks, with " + enabled + " being enabled.");
    }
}
