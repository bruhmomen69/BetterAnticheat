package better.anticheat.core.player.tracker.impl.confirmation;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class LyricManager {

    @Getter
    private final Map<String, LyricSequenceData> lyricSequenceDataMap = new HashMap<>();

    public LyricManager() {
        // Constructor
    }

    public LyricSequenceData getLyricSequenceData(final String artist, final String title, final int maxLines) {
        final var key = artist + "::" + title + "::" + maxLines;
        if (!lyricSequenceDataMap.containsKey(key)) {
            try {
                lyricSequenceDataMap.put(key, new LyricSequenceData(artist, title, maxLines));
                log.info("Loaded lyric sequence data for '{} - {}'.", artist, title);
            } catch (final Exception e) {
                log.warn("Failed to load lyric sequence data for '{} - {}': {}", artist, title, e.getMessage());
                return null;
            }
        }
        return lyricSequenceDataMap.get(key);
    }
}
