
package atomicedit.jarreading.blockmodels_v2;

import java.util.Map;

/**
 *
 * @author Justin Bonner
 */
public class HardcodedBlockModels {
    
    private static final String WATER_NAME = "block/water";
    private static final String WATER_JSON = "" +
        "{   \"parent\": \"block/block\",\n" +
        "    \"textures\": {\n" +
        "        \"all\": \"block/water_still\"\n" +
        "    },\n" +
        "    \"elements\": [\n" +
        "        {   \"from\": [ 0, 0, 0 ],\n" +
        "            \"to\": [ 16, 16, 16 ],\n" +
        "            \"faces\": {\n" +
        "                \"down\":  { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#all\",   \"cullface\": \"down\", \"tintindex\": 1 },\n" +
        "                \"up\":    { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#all\",   \"cullface\": \"up\", \"tintindex\": 1 },\n" +
        "                \"north\": { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#all\",   \"cullface\": \"north\", \"tintindex\": 1 },\n" +
        "                \"south\": { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#all\",   \"cullface\": \"south\", \"tintindex\": 1 },\n" +
        "                \"west\":  { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#all\",   \"cullface\": \"west\", \"tintindex\": 1 },\n" +
        "                \"east\":  { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#all\",   \"cullface\": \"east\", \"tintindex\": 1 }\n" +
        "            }\n" +
        "        }\n" +
        "    ]\n" +
        "}";
    
    private static final String LAVA_NAME = "block/lava";
    private static final String LAVA_JSON = "" +
        "{\n" +
        "    \"parent\": \"block/cube_all\",\n" +
        "    \"textures\": {\n" +
        "        \"all\": \"block/lava_still\"\n" +
        "    }\n" +
        "}";
    
    
    public static void addBlockModelPrecursors(Map<String, String> nameToJson, Map<String, BlockModelPrecursor> precursors){
        nameToJson.put(WATER_NAME, WATER_JSON);
        nameToJson.put(LAVA_NAME, LAVA_JSON);
        precursors.put(WATER_NAME, BlockModelParser.parseBlockModel(nameToJson, WATER_NAME));
        precursors.put(LAVA_NAME, BlockModelParser.parseBlockModel(nameToJson, LAVA_NAME));
    }
    
}
