package better.anticheat.core.player.tracker.impl.confirmation.cookie.allocator;

import better.anticheat.core.player.tracker.impl.confirmation.cookie.CookieIdAllocator;
import better.anticheat.core.player.tracker.impl.confirmation.cookie.LyricSequenceData;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class LyricCookieAllocator implements CookieIdAllocator {

    private final LyricSequenceData lyricSequenceData;
    private final AtomicInteger currentIndex;

    public LyricCookieAllocator(final LyricSequenceData lyricSequenceData) {
        if (lyricSequenceData == null) {
            throw new IllegalArgumentException("LyricSequenceData cannot be null");
        }
        this.lyricSequenceData = lyricSequenceData;
        this.currentIndex = new AtomicInteger(0);
        log.debug("Created LyricCookieAllocator with {} lyric lines of {} bytes each",
                lyricSequenceData.getAvailableLyricCount(), lyricSequenceData.getCookieIdLength());
    }

    @Override
    public byte[] allocateNext() {
        if (lyricSequenceData.getAvailableLyricCount() == 0) {
            throw new IllegalStateException("No lyric lines available for allocation.");
        }
        final var index = currentIndex.getAndUpdate(i -> (i + 1) % lyricSequenceData.getAvailableLyricCount());
        final var next = lyricSequenceData.getLyricLine(index);
        log.debug("\uD83C\uDFB5 {}", new String(next, StandardCharsets.UTF_8));
        return next;
    }

    @Override
    public boolean isValidCookieId(final byte[] cookieId) {
        return lyricSequenceData.isValidLyricLine(cookieId);
    }

    @Override
    public int getCookieIdLength() {
        return lyricSequenceData.getCookieIdLength();
    }
}
