package better.anticheat.core.player.tracker.impl.confirmation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for cookie ID allocators.
 * Contains the allocator type and its specific parameters.
 */
@Getter
@RequiredArgsConstructor
public class CookieAllocatorConfig {
    
    /**
     * The type of allocator to create.
     * Supported types: "sequential", "random", "timestamp", "file"
     */
    private final String type;
    
    /**
     * Type-specific parameters for the allocator.
     * Different allocator types use different parameters.
     */
    private final Map<String, Object> parameters;
    
    /**
     * Creates a new configuration with the specified type and no parameters.
     * 
     * @param type the allocator type
     */
    public CookieAllocatorConfig(String type) {
        this.type = type;
        this.parameters = new HashMap<>();
    }
    
    /**
     * Creates a default configuration for SequentialLongCookieAllocator.
     * 
     * @return a default configuration
     */
    public static CookieAllocatorConfig createDefault() {
        return new CookieAllocatorConfig("sequential");
    }
    
    /**
     * Creates a configuration for SequentialLongCookieAllocator with a custom start value.
     * 
     * @param startValue the starting value for the sequence
     * @return a sequential allocator configuration
     */
    public static CookieAllocatorConfig createSequential(long startValue) {
        Map<String, Object> params = new HashMap<>();
        params.put("startValue", startValue);
        return new CookieAllocatorConfig("sequential", params);
    }
    
    /**
     * Creates a configuration for RandomByteCookieAllocator.
     * 
     * @param cookieLength the length of generated cookie IDs in bytes
     * @param maxRetries maximum retries for uniqueness
     * @return a random allocator configuration
     */
    public static CookieAllocatorConfig createRandom(int cookieLength, int maxRetries) {
        Map<String, Object> params = new HashMap<>();
        params.put("cookieLength", cookieLength);
        params.put("maxRetries", maxRetries);
        return new CookieAllocatorConfig("random", params);
    }
    
    /**
     * Creates a configuration for TimestampBasedCookieAllocator.
     * 
     * @param randomBytesLength the number of random bytes to append to the timestamp
     * @return a timestamp allocator configuration
     */
    public static CookieAllocatorConfig createTimestamp(int randomBytesLength) {
        Map<String, Object> params = new HashMap<>();
        params.put("randomBytesLength", randomBytesLength);
        return new CookieAllocatorConfig("timestamp", params);
    }
    
    /**
     * Creates a configuration for FileBasedCookieAllocator.
     * 
     * @param filename the name of the file containing cookie sequences
     * @return a file allocator configuration
     */
    public static CookieAllocatorConfig createFile(String filename) {
        Map<String, Object> params = new HashMap<>();
        params.put("filename", filename);
        return new CookieAllocatorConfig("file", params);
    }

    /**
     * Creates a configuration for LyricCookieAllocator.
     *
     * @param artist the artist of the song
     * @param title the title of the song
     * @param maxLines the maximum number of lyric lines to use (0 for all)
     * @return a lyric allocator configuration
     */
    public static CookieAllocatorConfig createLyric(String artist, String title, int maxLines) {
        Map<String, Object> params = new HashMap<>();
        params.put("artist", artist);
        params.put("title", title);
        params.put("maxLines", maxLines);
        return new CookieAllocatorConfig("lyric", params);
    }
}
