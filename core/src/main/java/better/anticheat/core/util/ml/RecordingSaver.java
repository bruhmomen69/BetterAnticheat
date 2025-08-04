package better.anticheat.core.util.ml;

import better.anticheat.core.player.Player;
import lombok.Getter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class for saving player recording data to files.
 */
@Getter
public class RecordingSaver {
    
    private final Path recordingDirectory;
    
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
     * @param name The name of the file to save to (without extension)
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
            Files.writeString(recordingDirectory.resolve(name + ".json"), JSON.toJSONString(json), StandardCharsets.UTF_16LE);
        } else {
            final JSONObject json = new JSONObject();
            json.put("yaws", yawsArrays);
            json.put("offsets", offsetsArrays);
            json.put("enhancedOffsets", enhancedOffsetsArrays);
            Files.writeString(recordingDirectory.resolve(name + ".json"), JSON.toJSONString(json), StandardCharsets.UTF_16LE);
        }
    }
}
