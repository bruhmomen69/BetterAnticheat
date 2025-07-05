package better.anticheat.core.player.tracker.impl.confirmation;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.util.MathUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Manages the loading and storage of cookie sequence data from a file.
 * This class is responsible for parsing various formats (hex, decimal, base64)
 * and ensuring all cookie IDs are padded to a uniform length.
 * <p>
 * It first attempts to load the file from resources, then falls back to the file system.
 * </p>
 *
 * @author BetterAnticheat
 * @since 1.0.0
 */
@Getter
@Slf4j
public class CookieSequenceData {
    private static final String COOKIE_SEQUENCE_DIR = "cookiesequence";

    private final List<byte[]> cookieIds;
    private final int cookieIdLength;

    /**
     * Loads cookie sequence data from the specified filename.
     * It first tries to load from resources, then from the file system.
     *
     * @param filename The name of the file to load (e.g., "cookie_sequences.txt").
     * @throws RuntimeException if the file cannot be found or loaded, or if no valid cookie IDs are found.
     */
    public CookieSequenceData(String filename) {
        List<String> lines = loadFileLines(filename);
        if (lines.isEmpty()) {
            throw new IllegalStateException("No valid cookie IDs found in file: " + filename);
        }

        List<byte[]> parsedCookieIds = parseCookieLines(lines);
        if (parsedCookieIds.isEmpty()) {
            throw new IllegalStateException("No valid cookie IDs could be parsed from file: " + filename);
        }

        this.cookieIdLength = determineAndPadLength(parsedCookieIds);
        this.cookieIds = parsedCookieIds;

        log.info("Loaded {} cookie IDs from '{}'. Padded to {} bytes.",
                cookieIds.size(), filename, cookieIdLength);
    }

    /**
     * Loads all lines from the specified file, first checking resources, then the file system.
     *
     * @param filename The name of the file to load.
     * @return A list of strings, each representing a line from the file.
     * @throws RuntimeException if the file cannot be found or read.
     */
    private List<String> loadFileLines(String filename) {
        // Try loading from resources first
        try (final var is = BetterAnticheat.class.getClassLoader().getResourceAsStream(filename)) {
            if (is != null) {
                log.info("Loading cookie sequences from resource: {}", filename);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    return reader.lines().collect(Collectors.toList());
                }
            } else {
                log.warn("Resource '{}' not found. Attempting to load from file system.", filename);
            }
        } catch (IOException e) {
            log.error("Error reading resource file '{}': {}", filename, e.getMessage());
            throw new RuntimeException("Failed to read resource file: " + filename, e);
        }

        // Fallback to file system
        final var filePath = Paths.get(BetterAnticheat.getInstance().getDirectory().toAbsolutePath().toString(),
                COOKIE_SEQUENCE_DIR, filename);
        final var file = filePath.toFile();

        if (!file.exists()) {
            // Create the directory if it doesn't exist
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (parentDir.mkdirs()) {
                    log.info("Created directory: {}", parentDir.getAbsolutePath());
                } else {
                    log.error("Failed to create directory: {}", parentDir.getAbsolutePath());
                }
            }
            throw new RuntimeException("Cookie sequence file not found: " + filePath.toAbsolutePath());
        }

        try {
            log.info("Loading cookie sequences from file system: {}", filePath.toAbsolutePath());
            return Files.readAllLines(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error reading file system file '{}': {}", filePath.toAbsolutePath(), e.getMessage());
            throw new RuntimeException("Failed to read file system file: " + filePath.toAbsolutePath(), e);
        }
    }

    /**
     * Parses a list of string lines into a list of byte arrays, ignoring comments and empty lines.
     *
     * @param lines The list of string lines to parse.
     * @return A list of parsed cookie IDs as byte arrays.
     */
    private List<byte[]> parseCookieLines(List<String> lines) {
        List<byte[]> parsed = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue; // Ignore empty lines and comments
            }

            try {
                byte[] cookieId = parseCookieId(line);
                if (cookieId != null) {
                    parsed.add(cookieId);
                } else {
                    log.warn("Skipping invalid cookie ID format on line {}: '{}'", i + 1, line);
                }
            } catch (Exception e) {
                log.warn("Error parsing cookie ID on line {}: '{}' - {}", i + 1, line, e.getMessage());
            }
        }
        return parsed;
    }

    /**
     * Parses a single string line into a byte array, supporting hex, decimal, and base64 formats.
     *
     * @param line The string line to parse.
     * @return The parsed cookie ID as a byte array, or null if the format is not recognized.
     * @throws NumberFormatException if decimal parsing fails.
     */
    private byte @Nullable [] parseCookieId(String line) throws NumberFormatException {
        // Return comments
        if (line.startsWith("#")) {
            return null;
        }

        // Try hex format (e.g., "deadbeef", "de:ad:be:ef", "01 23 45")
        String cleanHex = line.replaceAll("[^0-9a-fA-F]", "");
        if (!cleanHex.isEmpty() && cleanHex.length() % 2 == 0) {
            try {
                return Hex.decodeHex(cleanHex);
            } catch (DecoderException e) {
                // Not a valid hex string, try other formats
            }
        }

        // Try decimal format (e.g., "222,173,190,239")
        if (line.matches("^[0-9, ]+$")) {
            try {
                String[] byteStrings = line.split(",");
                byte[] bytes = new byte[byteStrings.length];
                for (int i = 0; i < byteStrings.length; i++) {
                    bytes[i] = (byte) Integer.parseInt(byteStrings[i].trim());
                }
                return bytes;
            } catch (NumberFormatException e) {
                // Not a valid decimal byte string, try other formats
            }
        }

        // Try Base64 format (e.g., "3q2+7w==")
        if (Base64.isBase64(line) && line.endsWith("=")) {
            try {
                return Base64.decodeBase64(line);
            } catch (Exception e) {
                // Not a valid Base64 string, try other formats
            }
        }

        // Any String format
        return (line).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Determines the maximum length among all parsed cookie IDs and pads shorter ones with zeros.
     *
     * @param parsedCookieIds The list of parsed cookie IDs.
     * @return The uniform length after padding.
     */
    private int determineAndPadLength(List<byte[]> parsedCookieIds) {
        int maxLength = 0;
        for (byte[] cookieId : parsedCookieIds) {
            if (cookieId.length > maxLength) {
                maxLength = cookieId.length;
            }
        }

        for (int i = 0; i < parsedCookieIds.size(); i++) {
            byte[] original = parsedCookieIds.get(i);
            if (original.length < maxLength) {
                byte[] padded = Arrays.copyOf(original, maxLength);
                // No need to fill with zeros, Arrays.copyOf already does that for new elements
                parsedCookieIds.set(i, padded);
                log.debug("Padded cookie ID from {} to {} bytes.", original.length, maxLength);
            }
        }
        return maxLength;
    }

    /**
     * Retrieves a cookie ID by its index.
     *
     * @param index The index of the cookie ID to retrieve.
     * @return The cookie ID as a byte array.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    public byte[] getCookieId(int index) {
        // Return a clone to prevent external modification
        return cookieIds.get(index).clone();
    }

    /**
     * Gets the total number of available cookie IDs.
     *
     * @return The number of cookie IDs.
     */
    public int getAvailableCookieCount() {
        return cookieIds.size();
    }

    /**
     * Checks if the given byte array represents a valid cookie ID present in this sequence data.
     *
     * @param cookieId The byte array to validate.
     * @return True if the cookie ID is found in the sequence, false otherwise.
     */
    public boolean isValidCookieId(byte[] cookieId) {
        for (byte[] existingCookie : cookieIds) {
            if (Arrays.equals(existingCookie, cookieId)) {
                return true;
            }
        }
        return false;
    }
}