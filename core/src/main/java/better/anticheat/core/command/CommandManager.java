package better.anticheat.core.command;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.command.core.parameter.PlayerParameterType;
import better.anticheat.core.command.core.parameter.UserParameterType;
import better.anticheat.core.command.core.suggestion.PlayerNameSuggestionProvider;
import better.anticheat.core.command.impl.*;
import better.anticheat.core.configuration.ConfigSection;
import better.anticheat.core.configuration.ConfigurationFile;
import better.anticheat.core.player.Player;
import com.github.retrooper.packetevents.protocol.player.User;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;

import java.util.*;

public class CommandManager {

    private final BetterAnticheat plugin;
    private final List<Command> commands;
    private Lamp.Builder<CommandActor> builder;

    public CommandManager(BetterAnticheat plugin, Lamp.Builder<?> builder) {
        this.plugin = plugin;
        this.builder = (Lamp.Builder<CommandActor>) builder;

        /*
         * NOTE: Load order is important! Parent commands must be registered before their children!
         */
        commands = Arrays.asList(
                new BACCommand(plugin),
                new AlertsCommand(plugin),
                new MitigateCommand(plugin),
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
    public Lamp<?> load() {
        if (plugin.getLamp() != null) {
            plugin.getLamp().unregisterAllCommands();
        }

        // Configure and build Lamp. We make sure to only suggest injected players and users.
        builder = builder.parameterTypes((config) -> {
                    config.addParameterType(Player.class, new PlayerParameterType());
                    config.addParameterType(User.class, new UserParameterType());
                }
        );
        builder = builder.suggestionProviders((config) -> {
            config.addProvider(Player.class, new PlayerNameSuggestionProvider());
            config.addProvider(User.class, new PlayerNameSuggestionProvider());
        });
        final var lamp = builder.build();

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
                lamp.register(command.getOrphans().handler(command));
            }
        }

        for (String file : modified) configMap.get(file).save();

        plugin.getDataBridge().logInfo("Loaded " + commands.size() + " commands, with " + enabled + " being enabled.");

        return lamp;
    }
}
