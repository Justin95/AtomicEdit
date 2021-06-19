
package atomicedit.jarreading.blockmodels;

import atomicedit.utils.FileUtils;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author Justin Bonner
 */
public class HardcodedBlockModels {
    
    private static final String HARDCODED_JSON_PATH = "/data/hardcoded_block_models/";
    
    private static final String WATER_NAME = "block/water";
    private static final String WATER_JSON_NAME = "water.json";
    
    private static final String LAVA_NAME = "block/lava";
    private static final String LAVA_JSON_NAME = "lava.json";
    
    
    public static void addBlockModelPrecursors(Map<String, String> nameToJson, Map<String, BlockModelPrecursor> precursors){
        nameToJson.put(WATER_NAME, loadJson(WATER_JSON_NAME));
        nameToJson.put(LAVA_NAME, loadJson(LAVA_JSON_NAME));
        precursors.put(WATER_NAME, BlockModelParser.parseBlockModel(nameToJson, WATER_NAME));
        precursors.put(LAVA_NAME, BlockModelParser.parseBlockModel(nameToJson, LAVA_NAME));
    }
    
    private static String loadJson(String jsonName) {
        try {
            return FileUtils.readResourceFile(HARDCODED_JSON_PATH + jsonName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
}
