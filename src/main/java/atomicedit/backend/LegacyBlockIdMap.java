
package atomicedit.backend;

import atomicedit.utils.FileUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;

/**
 * Translate legacy (1.12 and prior) block ids into block states.
 * @author Justin Bonner
 */
public class LegacyBlockIdMap {
    
    private static final String LEGACY_IDS_FILE_PATH = "/data/legacy_block_ids.json";
    
    private static short[] legacyIdToInternalId;
    private static boolean initialized = false;
    
    public static void initialize() {
        if (initialized) {
            throw new RuntimeException("Multiple initializations");
        }
        initialized = true;
        String legacyJsonStr;
        try {
            legacyJsonStr = FileUtils.readResourceFile(LEGACY_IDS_FILE_PATH);
        } catch(IOException e) {
            throw new RuntimeException("Error in initialization.", e);
        }
        JsonArray legacyJson = new JsonParser().parse(legacyJsonStr).getAsJsonArray();
        for (int i = 0; i < legacyJson.size(); i++) {
            JsonObject mapping = legacyJson.get(i).getAsJsonObject();
            //TODO add block properties to json and fix json
        }
    }
    
    public static short getBlockIdFromLegacyId(short legacyId, short variantNibble) {
        throw new UnsupportedOperationException("TODO");
    }
    
}
