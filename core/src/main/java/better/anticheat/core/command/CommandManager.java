package better.anticheat.core.command;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.command.impl.AlertsCommand;
import better.anticheat.core.command.impl.BACCommand;
import better.anticheat.core.command.impl.RecordingCommand;
import better.anticheat.core.command.impl.ReloadCommand;
import better.anticheat.core.configuration.ConfigSection;
import better.anticheat.core.configuration.ConfigurationFile;

import java.util.*;

public class CommandManager {

    private final BetterAnticheat plugin;
    private final List<Command> commands;

    public CommandManager(BetterAnticheat plugin) {
        this.plugin = plugin;

        /*
         * NOTE: Load order is important! Parent commands must be registered before their children!
         */
        commands = Arrays.asList(
                new BACCommand(plugin),
                new AlertsCommand(plugin),
                new RecordingCommand(plugin),
                new ReloadCommand(plugin)
        );
    }

    public Collection<Command> getAllCommands() {
        return Collections.unmodifiableList(commands);
    }


    /**
     * Load all commands via their preferred configuration files.
     */
    public void load() {
        plugin.getLamp().unregisterAllCommands();
        Map<String, ConfigurationFile> configMap = new HashMap<>();
        Set<String> modified = new HashSet<>();
        int enabled = 0;
        for (Command command : commands) {
            // Ensure the check has a defined config in its CheckInfo.
            if (command.getConfig() == null) {
                plugin.getDataBridge().logWarning("Could not load " + command.getName() + " due to null config!");
                continue;
            }

            // Resolve the corresponding file.
            String fileName = command.getConfig().toLowerCase();
            ConfigurationFile file = configMap.get(fileName);
            if (file == null) {
                file = plugin.getFile(fileName + ".yml");
                file.load();
                configMap.put(fileName, file);
            }

            // Ensure the command is in the file.
            ConfigSection node = file.getRoot();
            if (!node.hasNode(command.getName())) {
                modified.add(fileName);
                node.addNode(command.getName());
            }
            node = node.getConfigSection(command.getName());

            // Load the check with its appropriate config.
            if (command.load(node)) modified.add(fileName);
            if (command.isEnabled()) {
                enabled++;
                // Register the command if
                plugin.getLamp().register(command.getOrphans().handler(command));
            }
        }

        for (String file : modified) configMap.get(file).save();

        plugin.getDataBridge().logInfo("Loaded " + commands.size() + " commands, with " + enabled + " being enabled.");
    }
}
