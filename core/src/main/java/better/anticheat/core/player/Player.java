package better.anticheat.core.player;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.DataBridge;
import better.anticheat.core.check.Check;
import better.anticheat.core.check.CheckManager;
import better.anticheat.core.player.tracker.impl.PositionTracker;
import better.anticheat.core.player.tracker.impl.RotationTracker;
import better.anticheat.core.player.tracker.impl.confirmation.ConfirmationTracker;
import better.anticheat.core.player.tracker.impl.confirmation.CookieIdAllocator;
import better.anticheat.core.player.tracker.impl.confirmation.allocator.*;
import better.anticheat.core.player.tracker.impl.entity.EntityTracker;
import better.anticheat.core.player.tracker.impl.ml.CMLTracker;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.player.User;
import lombok.Getter;

import lombok.Setter;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Player implements Closeable {

    @Getter
    private final User user;
    @Getter
    private final PositionTracker positionTracker;
    @Getter
    private final RotationTracker rotationTracker;
    @Getter
    private final ConfirmationTracker confirmationTracker;
    @Getter
    private final EntityTracker entityTracker;
    @Getter
    private final CMLTracker cmlTracker;

    @Getter
    private List<Check> checks = null;

    @Getter @Setter
    private boolean alerts = false;

    private final List<Closeable> closeables = new ArrayList<>();

    public Player(final User user, final DataBridge dataBridge) {
        this.user = user;
        this.positionTracker = new PositionTracker(this);
        this.rotationTracker = new RotationTracker(this);
        
        // Create a separate cookie allocator instance for this player
        var cookieAllocator = createCookieAllocator(
            BetterAnticheat.getInstance().getCookieAllocatorConfig()
        );
        this.confirmationTracker = new ConfirmationTracker(this, cookieAllocator);
        
        this.entityTracker = new EntityTracker(this, this.confirmationTracker, this.positionTracker, dataBridge);
        this.cmlTracker = new CMLTracker(this);
        load();

        closeables.add(dataBridge.registerTickListener(user, this.confirmationTracker::sendTickKeepaliveNoFlush));
    }

    /*
     * Handle packets.
     */
    public void handleReceivePacket(PacketPlayReceiveEvent event) {
        this.positionTracker.handlePacketPlayReceive(event);
        this.rotationTracker.handlePacketPlayReceive(event);
        this.confirmationTracker.handlePacketPlayReceive(event);
        this.entityTracker.handlePacketPlayReceive(event);
        this.cmlTracker.handlePacketPlayReceive(event);

        for (Check check : this.checks) {
            if (!check.isEnabled()) continue;
            check.handleReceivePlayPacket(event);
        }
    }

    public void handleSendPacket(PacketPlaySendEvent event) {
        this.positionTracker.handlePacketPlaySend(event);
        this.rotationTracker.handlePacketPlaySend(event);
        this.confirmationTracker.handlePacketPlaySend(event);
        this.entityTracker.handlePacketPlaySend(event);
        this.cmlTracker.handlePacketPlaySend(event);

        for (Check check : this.checks) {
            if (!check.isEnabled()) continue;
            check.handleSendPlayPacket(event);
        }
    }

    /*
     *
     */

    public void load() {
        if (checks == null) checks = BetterAnticheat.getInstance().getCheckManager().getChecks(this);
        else for (Check check : checks) check.load();

        // Load CML generated checks.
        this.cmlTracker.onPlayerInit();
    }

    @Override
    public void close() throws IOException {
        for (final var closeable : this.closeables) {
            if (closeable == null) continue;
            closeable.close();
        }
    }
    
    /**
     * Creates a new cookie ID allocator instance based on the provided configuration.
     * Each call creates a separate instance, ensuring per-player isolation.
     *
     * @param config the allocator configuration
     * @return a new CookieIdAllocator instance
     */
    private CookieIdAllocator createCookieAllocator(final better.anticheat.core.player.tracker.impl.confirmation.CookieAllocatorConfig config) {
        if (config == null) {
            return new SequentialLongCookieAllocator();
        }

        try {
            switch (config.getType().toLowerCase()) {
                case "sequential":
                case "sequential_long":
                    return createSequentialAllocator(config);

                case "random":
                case "random_byte":
                    return createRandomAllocator(config);

                case "timestamp":
                case "timestamp_based":
                    return createTimestampAllocator(config);

                case "file":
                case "file_based":
                    return createFileAllocator(config);

                case "lyric":
                case "lyric_based":
                    return createLyricAllocator(config);

                default:
                    return new SequentialLongCookieAllocator();
            }
        } catch (final Exception e) {
            // Fall back to default if there's any error
            return new SequentialLongCookieAllocator();
        }
    }
    
    /**
     * Creates a sequential cookie allocator from the given configuration.
     *
     * @param config The configuration for the allocator.
     * @return A new sequential cookie allocator.
     */
    private CookieIdAllocator createSequentialAllocator(final better.anticheat.core.player.tracker.impl.confirmation.CookieAllocatorConfig config) {
        final var params = config.getParameters();

        if (params.containsKey("startValue")) {
            final var startValueObj = params.get("startValue");
            long startValue;

            if (startValueObj instanceof Number) {
                startValue = ((Number) startValueObj).longValue();
            } else {
                startValue = Long.parseLong(startValueObj.toString());
            }

            return new SequentialLongCookieAllocator(startValue);
        }

        return new SequentialLongCookieAllocator();
    }
    
    /**
     * Creates a random byte cookie allocator from the given configuration.
     *
     * @param config The configuration for the allocator.
     * @return A new random byte cookie allocator.
     */
    private CookieIdAllocator createRandomAllocator(final better.anticheat.core.player.tracker.impl.confirmation.CookieAllocatorConfig config) {
        final var params = config.getParameters();

        final var cookieLength = getIntParameter(params, "cookieLength", 8);
        final var maxRetries = getIntParameter(params, "maxRetries", 100);

        return new RandomByteCookieAllocator(cookieLength, maxRetries);
    }
    
    /**
     * Creates a timestamp-based cookie allocator from the given configuration.
     *
     * @param config The configuration for the allocator.
     * @return A new timestamp-based cookie allocator.
     */
    private CookieIdAllocator createTimestampAllocator(final better.anticheat.core.player.tracker.impl.confirmation.CookieAllocatorConfig config) {
        final var params = config.getParameters();

        final var randomBytesLength = getIntParameter(params, "randomBytesLength", 8);

        return new TimestampBasedCookieAllocator(randomBytesLength);
    }
    
    /**
     * Creates a file-based cookie allocator from the given configuration.
     *
     * @param config The configuration for the allocator.
     * @return A new file-based cookie allocator.
     */
    private CookieIdAllocator createFileAllocator(final better.anticheat.core.player.tracker.impl.confirmation.CookieAllocatorConfig config) {
        final var cookieSequenceData = BetterAnticheat.getInstance().getCookieSequenceData();

        if (cookieSequenceData == null) {
            // Fall back to sequential if no cookie sequence data is available
            return new SequentialLongCookieAllocator();
        }

        return new FileBasedCookieAllocator(cookieSequenceData);
    }
    
    /**
     * Creates a lyric-based cookie allocator from the given configuration.
     *
     * @param config The configuration for the allocator.
     * @return A new lyric-based cookie allocator.
     */
    private CookieIdAllocator createLyricAllocator(final better.anticheat.core.player.tracker.impl.confirmation.CookieAllocatorConfig config) {
        final var params = config.getParameters();

        final var artist = (String) params.get("artist");
        final var title = (String) params.get("title");
        final var maxLines = getIntParameter(params, "maxLines", 0);

        if (artist == null || title == null) {
            throw new IllegalArgumentException("Artist and title must be specified for lyric cookie allocator.");
        }

        final var lyricSequenceData = BetterAnticheat.getInstance().getLyricManager().getLyricSequenceData(artist, title, maxLines);
        if (lyricSequenceData == null) {
            throw new IllegalStateException("Lyric sequence data not loaded for '" + artist + " - " + title + "'.");
        }

        return new LyricCookieAllocator(lyricSequenceData);
    }
    
    /**
     * Gets an integer parameter from a map, returning a default value if the key is not present or the value is not a valid integer.
     *
     * @param params       The map of parameters.
     * @param key          The key to look up.
     * @param defaultValue The default value to return if the key is not found or the value is invalid.
     * @return The parameter value or the default value.
     */
    private int getIntParameter(final java.util.Map<String, Object> params, final String key, final int defaultValue) {
        final var value = params.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value != null) {
            try {
                return Integer.parseInt(value.toString());
            } catch (final NumberFormatException e) {
                // Use default value if parsing fails
            }
        }
        return defaultValue;
    }
}