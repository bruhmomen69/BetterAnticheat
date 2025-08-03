package better.anticheat.core.player.tracker.impl.confirmation.cookie;

import com.alibaba.fastjson2.JSON;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class LyricSequenceData {

    private static final String LRCLIB_API_BASE = "https://lrclib.net/api/search";

    @Getter
    private final List<byte[]> lyricLines;
    @Getter
    private final int cookieIdLength;

    public LyricSequenceData(final String artist, final String title, final int maxLines) {
        this.lyricLines = fetchLyrics(artist, title, maxLines);
        if (this.lyricLines.isEmpty()) {
            throw new IllegalStateException("No lyrics found for artist: " + artist + ", title: " + title);
        }
        this.cookieIdLength = determineAndPadLength(this.lyricLines);
        log.info("Loaded {} lyric lines for '{} - {}'. Padded to {} bytes.",
                lyricLines.size(), artist, title, cookieIdLength);
    }

    private List<byte[]> fetchLyrics(final String artist, final String title, final int maxLines) {
        final var fetchedLines = new ArrayList<byte[]>();
        final var encodedArtist = URLEncoder.encode(artist, StandardCharsets.UTF_8);
        final var encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
        final var url = String.format("%s?artist_name=%s&track_name=%s", LRCLIB_API_BASE, encodedArtist, encodedTitle);

        final var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .build();

        try (final var client = HttpClient.newHttpClient()) {
            final var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                final var results = JSON.parseArray(response.body());

                if (results == null || results.isEmpty()) {
                    log.warn("Empty lyrics found for artist: {}, title: {}", artist, title);
                    return fetchedLines;
                }

                for (int ii = 0; ii < results.size(); ii++) {
                    final var result = results.getJSONObject(ii);
                    var lyrics = result.getString("plain_lyrics");

                    if (lyrics == null) {
                        lyrics = result.getString("plainLyrics");
                    }

                    if (lyrics == null) {
                        lyrics = result.getString("syncedLyrics");
                    }

                    if (lyrics == null || lyrics.isEmpty()) {
                        log.warn("Empty lyric block found for artist: {}, title: {}", artist, title);
                        continue;
                    }

                    final var lines = lyrics.split("\r?\n");
                    for (var i = 0; i < lines.length && (maxLines <= 0 || i < maxLines); i++) {
                        final var line = lines[i].trim();
                        if (line.isEmpty()) {
                            continue;
                        }

                        // Add size to ensure all sequences are unique.
                        fetchedLines.add((fetchedLines.size() + "-" + line).getBytes(StandardCharsets.UTF_8));
                    }
                }
            } else {
                log.error("Failed to fetch lyrics from lrclib.net. Status code: {}", response.statusCode());
            }
        } catch (final IOException | InterruptedException e) {
            log.error("Error fetching lyrics: {}", e.getMessage());
        }
        return fetchedLines;
    }

    private int determineAndPadLength(final List<byte[]> cookieIds) {
        var maxLength = 0;
        for (final var cookieId : cookieIds) {
            if (cookieId.length > maxLength) {
                maxLength = cookieId.length;
            }
        }

        for (var i = 0; i < cookieIds.size(); i++) {
            final var original = cookieIds.get(i);
            if (original.length < maxLength) {
                final var padded = Arrays.copyOf(original, maxLength);
                cookieIds.set(i, padded);
            }
        }
        return maxLength;
    }

    public byte[] getLyricLine(final int index) {
        return lyricLines.get(index).clone();
    }

    public boolean isValidLyricLine(final byte[] lyricLine) {
        for (final var existingLyric : lyricLines) {
            if (Arrays.equals(existingLyric, lyricLine)) {
                return true;
            }
        }
        return false;
    }

    public int getAvailableLyricCount() {
        return lyricLines.size();
    }
}
