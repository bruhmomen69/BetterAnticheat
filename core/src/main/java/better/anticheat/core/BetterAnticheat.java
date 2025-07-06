package better.anticheat.core;

import better.anticheat.core.check.CheckManager;
import better.anticheat.core.command.BetterAnticheatCommand;
import better.anticheat.core.configuration.ConfigSection;
import better.anticheat.core.configuration.ConfigurationFile;
import better.anticheat.core.player.PlayerManager;
import better.anticheat.core.player.tracker.impl.confirmation.CookieAllocatorConfig;
import better.anticheat.core.player.tracker.impl.confirmation.CookieSequenceData;
import better.anticheat.core.player.tracker.impl.confirmation.LyricManager;
import better.anticheat.core.util.ml.MLTrainer;
import better.anticheat.core.util.ml.ModelConfig;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class BetterAnticheat {

    @Getter
    private static BetterAnticheat instance;

    // Constructor-related objs
    private final DataBridge dataBridge;
    private final Path directory;
    private boolean enabled;

    // Managers
    private final CheckManager checkManager;
    private final LyricManager lyricManager;
    private final PlayerManager playerManager;

    // Settings
    private int alertCooldown;
    private List<String> alertHover;
    private String alertMessage, alertPermission, clickCommand;
    private boolean punishmentModulo, testMode, useCommand;
    private Map<String, ModelConfig> modelConfigs = new HashMap<>();
    private boolean mlCombatDamageEnabled;
    private double mlCombatDamageThreshold;
    private double mlCombatDamageCancellationMultiplier;
    private double mlCombatDamageReductionMultiplier;
    private boolean velocityTickCheckEnabled;
    private int minTicksSinceLastAttack;
    private double minAverageForTickCheck;
    private CookieAllocatorConfig cookieAllocatorConfig;
    private CookieSequenceData cookieSequenceData;

    public BetterAnticheat(DataBridge dataBridge, Path directory) {
        this.dataBridge = dataBridge;
        this.directory = directory;
        this.enabled = true;

        instance = this;

        this.checkManager = new CheckManager(this);
        this.lyricManager = new LyricManager();
        this.playerManager = new PlayerManager(this);

        /*
         * We only support 1.21+.
         */
        if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_21)) {
            dataBridge.logWarning("You are running on an unsupported version of Minecraft!");
            dataBridge.logWarning("Please update to 1.21 or above!");
            enabled = false;
        }
    }

    public void enable() {
        if (!enabled) return;
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListener(this));
        load();

        // Ensure players are 1.21.4+.
        playerManager.registerQuantifier((user -> user.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_4)));

        if (useCommand) {
            dataBridge.registerCommands(null, null, new BetterAnticheatCommand(this.dataBridge, this.directory));
        }
    }

    public void disable() {
        enabled = false;
    }

    public void load() {
        if (!enabled) return;

        dataBridge.logInfo("Beginning load!");

        ConfigSection settings = getFile("settings.yml", BetterAnticheat.class.getResourceAsStream("/settings.yml")).load();
        alertCooldown = settings.getObject(Integer.class, "alert-cooldown", 1000);
        alertPermission = settings.getObject(String.class, "alert-permission", "better.anticheat");
        alertHover = settings.getList(String.class, "alert-hover");
        alertMessage = settings.getObject(String.class, "alert-message", "");
        clickCommand = settings.getObject(String.class, "click-command", "");
        punishmentModulo = settings.getObject(Boolean.class, "punishment-modulo", true);
        testMode = settings.getObject(Boolean.class, "test-mode", false);
        useCommand = settings.getObject(Boolean.class, "enable-commands", true); // Default to true for people who have not updated their config.

        loadML(settings);
        loadCookieAllocator(settings);

        checkManager.load();
        playerManager.load();

        dataBridge.logInfo("Load finished!");
    }

    public void loadML(final ConfigSection baseConfig) {
        final var mlNode = baseConfig.getConfigSection("ml");
        final var mlEnabled = mlNode.getObject(Boolean.class, "enabled", false);

        final var models = mlNode.getConfigSection("models");

        if (mlEnabled) {
            for (final var child : models.getChildren()) {
                modelConfigs.put(child.getKey(), new ModelConfig(
                        child.getObject(String.class, "displayName", "example-model"),
                        child.getObject(String.class, "type", "model-type"),
                        child.getObject(Integer.class, "slice", 1),
                        child.getList(String.class, "legitDatasetNames"),
                        child.getList(String.class, "cheatDatasetNames"),
                        child.getObject(Boolean.class, "statistics", false),
                        child.getObject(Boolean.class, "shrink", true),
                        child.getObject(Integer.class, "samples", 10),
                        child.getObject(Double.class, "threshold", 7.5),
                        child
                ));
            }
        }

        modelConfigs.forEach((name, config) -> {
            try {
                dataBridge.logInfo("Loading model for " + name + "...");
                final var model = MLTrainer.create(config.getLegitDatasetNames(), config.getCheatDatasetNames(), config.getType(), config.getSlice(), config.isStatistics(), config.isStatistics(), config.isShrink(), this.directory);
                config.setClassifierFunction(model);
                dataBridge.logInfo("Model for " + name + " loaded!");
            } catch (IOException e) {
                dataBridge.logInfo("Error while creating model trainer for " + name + ": " + e.getMessage());
                e.printStackTrace();
            }
        });

        final var mlCombatNode = mlNode.getConfigSection("combat-damage-mitigation");

        if (mlCombatNode == null) {
            // Default values if configuration section doesn't exist
            this.mlCombatDamageEnabled = true;
            this.mlCombatDamageThreshold = 7.0;
            this.mlCombatDamageCancellationMultiplier = 3.0;
            this.mlCombatDamageReductionMultiplier = 5.0;
            this.velocityTickCheckEnabled = true;
            this.minTicksSinceLastAttack = 3;
            this.minAverageForTickCheck = 8.0;
        } else {
            this.mlCombatDamageEnabled = mlCombatNode.getObject(Boolean.class, "enabled", false);
            this.mlCombatDamageThreshold = mlCombatNode.getObject(Double.class, "threshold", 5.0);
            this.mlCombatDamageCancellationMultiplier = mlCombatNode.getObject(Double.class, "cancellation-multiplier", 3.0);
            this.mlCombatDamageReductionMultiplier = mlCombatNode.getObject(Double.class, "damage-reduction-multiplier", 5.0);

            final var velocityTickCheckNode = mlCombatNode.getConfigSection("velocity-tick-check");
            if (velocityTickCheckNode != null) {
                this.velocityTickCheckEnabled = velocityTickCheckNode.getObject(Boolean.class, "enabled", true);
                this.minTicksSinceLastAttack = velocityTickCheckNode.getObject(Integer.class, "min-ticks-since-last-attack", 3);
                this.minAverageForTickCheck = velocityTickCheckNode.getObject(Double.class, "min-average-for-tick-check", 7.5);
            } else {
                this.velocityTickCheckEnabled = false;
                this.minTicksSinceLastAttack = 3;
                this.minAverageForTickCheck = 7.5;
            }

            dataBridge.logInfo("Loaded ML combat damage configuration: enabled=" + mlCombatDamageEnabled +
                    ", threshold=" + mlCombatDamageThreshold +
                    ", cancellation-multiplier=" + mlCombatDamageCancellationMultiplier +
                    ", damage-reduction-multiplier=" + mlCombatDamageReductionMultiplier +
                    ", velocity-tick-check-enabled=" + velocityTickCheckEnabled +
                    ", max-ticks-since-last-attack=" + minTicksSinceLastAttack +
                    ", min-average-for-tick-check=" + minAverageForTickCheck);
        }
    }

    /**
     * Loads the cookie allocator configuration from the provided base configuration.
     *
     * @param baseConfig The base configuration section.
     */
    public void loadCookieAllocator(final ConfigSection baseConfig) {
        final var cookieNode = baseConfig.getConfigSection("cookie-allocator");

        if (cookieNode == null) {
            dataBridge.logInfo("No cookie allocator configuration found, using default sequential allocator");
            this.cookieAllocatorConfig = CookieAllocatorConfig.createDefault();
            return;
        }

        final var type = cookieNode.getObject(String.class, "type", "sequential");
        final var parametersNode = cookieNode.getConfigSection("parameters");

        final Map<String, Object> parameters = new HashMap<>();
        if (parametersNode != null) {
            // For file-based allocator
            if (parametersNode.hasNode("filename")) {
                parameters.put("filename", parametersNode.getObject(String.class, "filename", "cookie_sequences.txt"));
            }

            // For sequential allocator
            if (parametersNode.hasNode("startValue")) {
                parameters.put("startValue", parametersNode.getObject(Long.class, "startValue", 0L));
            }

            // For random allocator
            if (parametersNode.hasNode("cookieLength")) {
                parameters.put("cookieLength", parametersNode.getObject(Integer.class, "cookieLength", 8));
            }
            if (parametersNode.hasNode("maxRetries")) {
                parameters.put("maxRetries", parametersNode.getObject(Integer.class, "maxRetries", 100));
            }

            // For timestamp allocator
            if (parametersNode.hasNode("randomBytesLength")) {
                parameters.put("randomBytesLength", parametersNode.getObject(Integer.class, "randomBytesLength", 4));
            }

            // For lyric allocator
            if (parametersNode.hasNode("artist")) {
                parameters.put("artist", parametersNode.getObject(String.class, "artist", ""));
            }
            if (parametersNode.hasNode("title")) {
                parameters.put("title", parametersNode.getObject(String.class, "title", ""));
            }
            if (parametersNode.hasNode("maxLines")) {
                parameters.put("maxLines", parametersNode.getObject(Integer.class, "maxLines", 0));
            }
        }

        this.cookieAllocatorConfig = new CookieAllocatorConfig(type, parameters);

        // If this is a file-based allocator, load the cookie sequence data during initialization
        if ("file".equalsIgnoreCase(type) || "file_based".equalsIgnoreCase(type)) {
            final var filename = (String) parameters.getOrDefault("filename", "cookie_sequences.txt");
            try {
                this.cookieSequenceData = new CookieSequenceData(filename);
                dataBridge.logInfo("Loaded cookie sequence data for file-based allocator: " + filename);
            } catch (final Exception e) {
                dataBridge.logWarning("Failed to load cookie sequence data for file '" + filename + "': " + e.getMessage());
                dataBridge.logWarning("Falling back to default sequential allocator");
                this.cookieAllocatorConfig = CookieAllocatorConfig.createTimestamp(10);
                this.cookieSequenceData = null;
            }
        } else if ("lyric".equalsIgnoreCase(type) || "lyric_based".equalsIgnoreCase(type)) {
            final var artist = (String) parameters.get("artist");
            final var title = (String) parameters.get("title");
            final var maxLines = (Integer) parameters.getOrDefault("maxLines", 0);

            if (artist == null || title == null) {
                dataBridge.logWarning("Artist and title must be specified for lyric cookie allocator. Falling back to default sequential allocator.");
                this.cookieAllocatorConfig = CookieAllocatorConfig.createTimestamp(10);
                return;
            }

            final var lyrics = this.lyricManager.getLyricSequenceData(artist, title, maxLines);
            if (lyrics == null) {
                dataBridge.logWarning("Lyric sequence data not loaded for '" + artist + " - " + title + "'. Falling back to default sequential allocator.");
                this.cookieAllocatorConfig = CookieAllocatorConfig.createTimestamp(10);
                return;
            }

            if (lyrics.getAvailableLyricCount() < 50) {
                dataBridge.logWarning("Not enough lyric sequences available for '" + artist + " - " + title + "'. Consider choosing a different song, or setting maxLines to 0.");
                this.cookieAllocatorConfig = CookieAllocatorConfig.createSequential(Integer.MIN_VALUE / 2);
                return;
            }
        }

        dataBridge.logInfo("Loaded cookie allocator configuration: type=" + type + ", parameters=" + parameters);
    }

    public ConfigurationFile getFile(String name) {
        return new ConfigurationFile(name, directory);
    }

    public ConfigurationFile getFile(String name, InputStream input) {
        return new ConfigurationFile(name, directory, input);
    }

}
