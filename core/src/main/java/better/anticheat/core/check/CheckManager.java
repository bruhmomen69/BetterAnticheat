package better.anticheat.core.check;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.check.impl.chat.HiddenChatCheck;
import better.anticheat.core.check.impl.chat.ImpossibleCompletionCheck;
import better.anticheat.core.check.impl.chat.ImpossibleMessageCheck;
import better.anticheat.core.check.impl.combat.*;
import better.anticheat.core.check.impl.dig.DigBlockFacePositionCheck;
import better.anticheat.core.check.impl.dig.DigOrderCheck;
import better.anticheat.core.check.impl.dig.MultiBreakCheck;
import better.anticheat.core.check.impl.dig.RepeatedDigCheck;
import better.anticheat.core.check.impl.flying.*;
import better.anticheat.core.check.impl.heuristic.CombatAccelerationCheck;
import better.anticheat.core.check.impl.heuristic.LinearAimDeviationCheck;
import better.anticheat.core.check.impl.misc.*;
import better.anticheat.core.check.impl.packet.BalanceCheck;
import better.anticheat.core.check.impl.packet.PostCheck;
import better.anticheat.core.check.impl.place.CursorPositionCheck;
import better.anticheat.core.check.impl.place.PlaceBlockFacePositionCheck;
import better.anticheat.core.configuration.ConfigSection;
import better.anticheat.core.configuration.ConfigurationFile;
import better.anticheat.core.player.Player;

import java.util.*;

public class CheckManager {

    private final BetterAnticheat plugin;
    private final List<Check> checks;

    public CheckManager(BetterAnticheat plugin) {
        this.plugin = plugin;

        checks = Arrays.asList(
                // Chat Checks
                new HiddenChatCheck(plugin),
                new ImpossibleCompletionCheck(plugin),
                new ImpossibleMessageCheck(plugin),

                // Combat Checks
                new ActionInteractOrderCheck(plugin),
                new DualClickCheck(plugin),
                new InvalidInteractionPositionCheck(plugin),
                new InvalidReleaseValuesCheck(plugin),
                new InvalidUseActionsCheck(plugin),
                new MultipleActionCheck(plugin),
                new MultipleHitCheck(plugin),
                new NoSwingCombatCheck(plugin),
                new SelfHitCheck(plugin),
                new SlotInteractOrderCheck(plugin),

                // Dig Checks
                new DigBlockFacePositionCheck(plugin),
                new DigOrderCheck(plugin),
                new MultiBreakCheck(plugin),
                new RepeatedDigCheck(plugin),

                // Flying Checks
                new ArtificialFlyingCheck(plugin),
                new ArtificialPositionCheck(plugin),
                new FlyingSequenceCheck(plugin),
                new ImpossiblePositionCheck(plugin),
                new ImpossibleRotationCheck(plugin),
                new RepeatedRotationCheck(plugin),
                new RepeatedSteerCheck(plugin),

                // Heuristic Checks
                new CombatAccelerationCheck(plugin),
                new LinearAimDeviationCheck(plugin),

                // Misc Checks
                new ImpossibleHorseJumpCheck(plugin),
                new ImpossibleSlotCheck(plugin),
                new LargeNameCheck(plugin),
                new MultipleSlotCheck(plugin),
                new SmallRenderCheck(plugin),

                // Packet Checks
                new BalanceCheck(plugin),
                new PostCheck(plugin),

                // Place Checks
                new CursorPositionCheck(plugin),
                new PlaceBlockFacePositionCheck(plugin)
        );
    }

    public Collection<Check> getAllChecks() {
        return Collections.unmodifiableList(checks);
    }

    public List<Check> getChecks(Player player) {
        /*
         * Do NOT return the existing array list. That would lead to multiple users using the same list, creating
         * concurrency issues.
         * The advantage of this method is also that cloning does not call the constructor, meaning that only the
         * original copies present in the CHECKS list will be reloaded.
         */
        List<Check> returnList = new ArrayList<>();
        for (Check check : checks) {
            returnList.add(check.initialCopy(player));
        }

        return returnList;
    }

    /**
     * Load all checks in the CHECKS list via their preferred configuration files.
     */
    public void load() {
        Map<String, ConfigurationFile> configMap = new HashMap<>();
        Set<String> modified = new HashSet<>();
        int enabled = 0;
        for (Check check : checks) {
            // Ensure the check has a defined config in its CheckInfo.
            if (check.getConfig() == null) {
                plugin.getDataBridge().logWarning("Could not load " + check.getName() + " due to null config!");
                continue;
            }

            // Resolve the corresponding file.
            String fileName = check.getConfig().toLowerCase();
            ConfigurationFile file = configMap.get(fileName);
            if (file == null) {
                file = plugin.getFile(fileName + ".yml");
                file.load();
                configMap.put(fileName, file);
            }

            // Ensure the category is in the file.
            ConfigSection node = file.getRoot();
            if (!node.hasNode(check.getCategory())) {
                modified.add(fileName);
                node.addNode(check.getCategory());
            }
            node = node.getConfigSection(check.getCategory());

            // Ensure the check is in the file
            if (!node.hasNode(check.getName())) {
                modified.add(fileName);
                node.addNode(check.getName());
            }
            node = node.getConfigSection(check.getName());

            // Load the check with its appropriate config.
            if (check.load(node)) modified.add(fileName);
            if (check.isEnabled()) enabled++;

            // Assign to default punishment group if category group doesn't exist.
            if (check.getPunishmentGroups().isEmpty()) {
                List<String> punishmentGroupNames = new ArrayList<>();
                if (plugin.getPunishmentManager().getPunishmentGroup(check.getCategory()) != null) {
                    punishmentGroupNames.add(check.getCategory());
                } else {
                    punishmentGroupNames.add("default");
                }
                node.setList(String.class, "punishment-groups", punishmentGroupNames);
                modified.add(fileName);
            }
        }

        for (String file : modified) configMap.get(file).save();
        plugin.getDataBridge().logInfo("Loaded " + checks.size() + " checks, with " + enabled + " being enabled.");
    }
}