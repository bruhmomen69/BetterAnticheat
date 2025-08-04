package better.anticheat.core.util.ml;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.player.Player;
import lombok.Getter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for saving player recording data to files.
 */
@Getter
public class RecordingSaver {

    private final Path recordingDirectory;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public RecordingSaver(Path directory) {
        this.recordingDirectory = directory.resolve("recording");
        if (!recordingDirectory.toFile().exists()) {
            recordingDirectory.toFile().mkdirs();
        }
    }

    /**
     * Saves a player's recording data to a specified file name.
     * 
     * @param player The player whose data should be saved
     * @param name   The name of the file to save to (without extension)
     * @throws IOException If there's an error reading or writing files
     */
    public void savePlayerData(Player player, String name) throws IOException {
        final var yawsArrays = new JSONArray();
        final var offsetsArrays = new JSONArray();
        final var enhancedOffsetsArrays = new JSONArray();

        for (double[][] doubles : player.getCmlTracker().getRecording()) {
            final var yawsArray = new JSONArray();
            for (double v : doubles[0]) {
                yawsArray.add(v);
            }
            yawsArrays.add(yawsArray);

            final var offsetsArray = new JSONArray();
            for (double v : doubles[1]) {
                offsetsArray.add(v);
            }
            offsetsArrays.add(offsetsArray);

            final var enhancedOffsetsArray = new JSONArray();
            for (double v : doubles[2]) {
                enhancedOffsetsArray.add(v);
            }
            enhancedOffsetsArrays.add(enhancedOffsetsArray);
        }

        final var exists = recordingDirectory.resolve(name + ".json").toFile().exists();
        if (exists) {
            final var bytes = Files.readAllBytes(recordingDirectory.resolve(name + ".json"));
            final var json = JSON.parseObject(new String(bytes, StandardCharsets.UTF_16LE));
            final var oldYawsArrays = json.getJSONArray("yaws");
            final var oldOffsetsArrays = json.getJSONArray("offsets");
            final var oldEnhancedOffsetsArrays = json.getJSONArray("enhancedOffsets");

            oldYawsArrays.addAll(yawsArrays);
            oldOffsetsArrays.addAll(offsetsArrays);
            oldEnhancedOffsetsArrays.addAll(enhancedOffsetsArrays);
            json.put("yaws", oldYawsArrays);
            json.put("offsets", oldOffsetsArrays);
            json.put("enhancedOffsets", oldEnhancedOffsetsArrays);
            Files.writeString(recordingDirectory.resolve(name + ".json"), JSON.toJSONString(json),
                    StandardCharsets.UTF_16LE);
        } else {
            final JSONObject json = new JSONObject();
            json.put("yaws", yawsArrays);
            json.put("offsets", offsetsArrays);
            json.put("enhancedOffsets", enhancedOffsetsArrays);
            Files.writeString(recordingDirectory.resolve(name + ".json"), JSON.toJSONString(json),
                    StandardCharsets.UTF_16LE);
        }

        // Send the recording via Discord webhook if configured
        sendRecordingWebhook(name + ".json", recordingDirectory.resolve(name + ".json"));
    }

    /**
     * Sends a recording file via Discord webhook if a valid webhook URL is
     * configured.
     * 
     * @param fileName The name of the file being sent
     * @param filePath The path to the file being sent
     */
    private void sendRecordingWebhook(String fileName, Path filePath) {
        final var plugin = BetterAnticheat.getInstance();
        final var webhookUrl = plugin.getWebhookUrl();

        if (webhookUrl == null || webhookUrl.isEmpty()) {
            return;
        }

        try {
            // Create multipart form data for file upload
            final var boundary = "----RecordingBoundary" + System.currentTimeMillis();

            // Create the JSON payload for the message
            final var data = new HashMap<String, String>();
            data.put("content", "Saved recording data for " + fileName);
            final var jsonPayload = JSON.toJSONString(data);

            // Build the multipart request body
            final var bodyBuilder = new StringBuilder();

            // Add JSON payload
            bodyBuilder.append("--").append(boundary).append("\r\n");
            bodyBuilder.append("Content-Disposition: form-data; name=\"payload_json\"\r\n");
            bodyBuilder.append("Content-Type: application/json\r\n\r\n");
            bodyBuilder.append(jsonPayload).append("\r\n");

            // Add file attachment
            bodyBuilder.append("--").append(boundary).append("\r\n");
            bodyBuilder.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                    .append(fileName).append("\"\r\n");
            bodyBuilder.append("Content-Type: application/json\r\n\r\n");

            // Read file content
            final var fileContent = Files.readString(filePath, StandardCharsets.UTF_16LE);
            bodyBuilder.append(fileContent).append("\r\n");

            // End boundary
            bodyBuilder.append("--").append(boundary).append("--\r\n");

            final var request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofString(bodyBuilder.toString()))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            plugin.getDataBridge().logWarning("Failed to send recording webhook: " + e.getMessage());
        }
    }
}
