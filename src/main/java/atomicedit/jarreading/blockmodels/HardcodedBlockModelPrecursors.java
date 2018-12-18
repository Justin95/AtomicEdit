
package atomicedit.jarreading.blockmodels;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Justin Bonner
 */
public class HardcodedBlockModelPrecursors {
    
    static final Map<String, BlockModelDataPrecursor> HARDCODED_BLOCK_MODEL_PRECURSORS = new HashMap<>();
    
    private static final BlockModelDataPrecursor WATER;
    static{
        String json = "" +
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
        WATER = BlockModelDataParser.parseJson(json);
    }
    
    private static final BlockModelDataPrecursor LAVA;
    static{
        String json = "" +
            "{\n" +
            "    \"parent\": \"block/cube_all\",\n" +
            "    \"textures\": {\n" +
            "        \"all\": \"block/lava_still\"\n" +
            "    }\n" +
            "}";
        LAVA = BlockModelDataParser.parseJson(json);
    }
    
    static{
        HARDCODED_BLOCK_MODEL_PRECURSORS.put("block/water", WATER);
        HARDCODED_BLOCK_MODEL_PRECURSORS.put("block/lava", LAVA);
    }
    
    
    
}
