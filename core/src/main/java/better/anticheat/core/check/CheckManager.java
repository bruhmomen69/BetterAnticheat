package better.anticheat.core.check;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.check.impl.chat.HiddenChatCheck;
import better.anticheat.core.check.impl.chat.ImpossibleCompletionCheck;
import better.anticheat.core.check.impl.chat.ImpossibleMessageCheck;
import better.anticheat.core.check.impl.combat.*;
import better.anticheat.core.check.impl.dig.DigBlockFacePositionCheck;
import better.anticheat.core.check.impl.dig.DigOrderCheck;
import better.anticheat.core.check.impl.dig.MultiBreakCheck;
import better.anticheat.core.check.impl.flying.*;
import better.anticheat.core.check.impl.heuristic.CombatAccelerationCheck;
import better.anticheat.core.check.impl.misc.*;
import better.anticheat.core.check.impl.packet.BalanceCheck;
import better.anticheat.core.check.impl.packet.PostCheck;
import better.anticheat.core.check.impl.place.PlaceBlockFacePositionCheck;
import better.anticheat.core.configuration.ConfigSection;
import better.anticheat.core.player.Player;

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
                new ActionInteractOrderCheck(),
                new DualClickCheck(),
                new InvalidReleaseValuesCheck(),
                new InvalidUseActionsCheck(),
                new MultipleActionCheck(),
                new MultipleHitCheck(),
                new NoSwingCombatCheck(),
                new SelfHitCheck(),
                new SlotInteractOrderCheck(),

                // Dig Checks
                new DigBlockFacePositionCheck(),
                new DigOrderCheck(),
                new MultiBreakCheck(),

                // Flying Checks
                new ArtificialFlyingCheck(),
                new ArtificialPositionCheck(),
                new FlyingSequenceCheck(),
                new ImpossiblePositionCheck(),
                new ImpossibleRotationCheck(),
                new RepeatedRotationCheck(),
                new RepeatedSteerCheck(),

                // Heuristic Checks
                new CombatAccelerationCheck(),

                // Misc Checks
                new ImpossibleHorseJumpCheck(),
                new ImpossibleSlotCheck(),
                new LargeNameCheck(),
                new MultipleSlotCheck(),
                new SmallRenderCheck(),

                // Packet Checks
                new BalanceCheck(),
                new PostCheck(),

                // Place Checks
                new PlaceBlockFacePositionCheck()
        );
    }

    public static Collection<Check> getAllChecks() {
        return Collections.unmodifiableList(CHECKS);
    }

    public static List<Check> getChecks(Player player) {
        /*
         * Do NOT return the existing array list. That would lead to multiple users using the same list, creating
         * concurrency issues.
         * The advantage of this method is also that cloning does not call the constructor, meaning that only the
         * original copies present in the CHECKS list will be reloaded.
         */
        List<Check> returnList = new ArrayList<>();
        for (Check check : CHECKS) {
            returnList.add(check.initialCopy(player));
        }

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
