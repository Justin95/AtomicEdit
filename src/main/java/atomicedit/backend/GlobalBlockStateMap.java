
package atomicedit.backend;

import atomicedit.backend.lighting.LightingBehavior;
import atomicedit.logging.Logger;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Justin Bonner
 */
public class GlobalBlockStateMap {
    
    private static final Map<BlockState, Short> blockToIdMap = new HashMap<>();
    private static final ArrayList<BlockState> idToBlockTypeMap = new ArrayList<>(100);
    private static short idCounter = 0;
    
    public static void addBlockType(BlockState blockType){
        if(idToBlockTypeMap.contains(blockType)){
            return;
        }
        if(idToBlockTypeMap.size() != idCounter){
            Logger.error("Block type map id desync");
        }
        synchronized(idToBlockTypeMap){
            idToBlockTypeMap.add(blockType);
        }
        blockToIdMap.put(blockType, idCounter);
        idCounter++;
    }
    
    public static boolean hasBlockType(BlockState blockType){
        return idToBlockTypeMap.contains(blockType);
    }
    
    public static short getBlockId(BlockState blockType){
        return blockToIdMap.get(blockType);
    }
    
    public static BlockState getBlockType(short id){
        if(id < 0 || id >= idCounter) throw new IllegalArgumentException("Bad id used in looking up mapping: " + id);
        return idToBlockTypeMap.get(id);
    }
    
    public static List<BlockState> getBlockTypes(){
        synchronized(idToBlockTypeMap){
            return Collections.unmodifiableList(idToBlockTypeMap);
        }
    }
    
    public static void debugPrintAllBlockStates() {
        JsonArray blockStatesJson = new JsonArray();
        idToBlockTypeMap.stream().sorted(
            Comparator.comparing((blockState) -> blockState.name)
        ).forEach((blockState) -> {
            JsonObject stateJson = new JsonObject();
            stateJson.addProperty("name", blockState.name);
            if (blockState.blockStateProperties != null) {
                JsonArray propJsonArray = new JsonArray();
                for (BlockStateProperty property : blockState.blockStateProperties) {
                    JsonObject propJson = new JsonObject();
                    propJson.addProperty("name", property.NAME);
                    switch(property.valueType) {
                        case STRING:
                            propJson.addProperty("type", "string");
                            propJson.addProperty("value", (String)property.VALUE);
                            break;
                        case INTEGER:
                            propJson.addProperty("type", "int");
                            propJson.addProperty("value", (Integer)property.VALUE);
                            break;
                        case BOOLEAN:
                            propJson.addProperty("type", "boolean");
                            propJson.addProperty("value", (Boolean)property.VALUE);
                            break;
                        default:
                            throw new RuntimeException("Missed switch on BlockStatePropertyType enum: " + property.valueType);
                    }
                    propJsonArray.add(propJson);
                }
                stateJson.add("properties", propJsonArray);
            }
            if (!blockState.lightingBehavior.equals(LightingBehavior.DEFAULT)) {
                JsonObject lightJson = new JsonObject();
                lightJson.addProperty("emit_light_level", blockState.lightingBehavior.emitLevel);
                lightJson.addProperty("allow_block_light", blockState.lightingBehavior.allowBlockLight);
                lightJson.addProperty("allow_sky_light", blockState.lightingBehavior.allowSkyLight);
                stateJson.add("light_behavior", lightJson);
            }
            blockStatesJson.add(stateJson);
        });
        //Logger.info(new GsonBuilder().setPrettyPrinting().create().toJson(blockStatesJson));
        Logger.info("Num Blockstates: " + idToBlockTypeMap.size());
    }
    
}
