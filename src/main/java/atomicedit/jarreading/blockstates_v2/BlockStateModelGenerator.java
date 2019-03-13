
package atomicedit.jarreading.blockstates_v2;

import atomicedit.backend.BlockState;
import atomicedit.jarreading.blockmodels_v2.BlockModel;
import atomicedit.jarreading.blockmodels_v2.BlockModelLookup;
import atomicedit.logging.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.joml.Vector3f;

/**
 *
 * @author Justin Bonner
 */
public class BlockStateModelGenerator {
    
    private final Map<Condition, BlockStateModel> conditionsToModels;
    
    private BlockStateModelGenerator(Map<Condition, BlockStateModel> conditionsToModels){
        this.conditionsToModels = conditionsToModels;
    }
    
    /**
     * Create a BlockStateModelGenerator from a block state json.
     * The block state json must comply with the specs here:
     * https://minecraft.gamepedia.com/Model
     * @param blockName
     * @param blockStateJson
     * @return 
     */
    public static BlockStateModelGenerator getInstance(String blockName, String blockStateJson){
        Map<Condition, BlockStateModel> conditionsToModels = createConditionMap(blockStateJson);
        return new BlockStateModelGenerator(conditionsToModels);
    }
    
    public List<BlockStateModel> generateBlockStateModel(BlockState blockState){
        ArrayList<BlockStateModel> models = new ArrayList<>();
        for(Condition check : conditionsToModels.keySet()){
            if(check.checkCondition(blockState)){
                models.add(conditionsToModels.get(check));
            }
        }
        models.trimToSize(); //minimize extra storage
        return models;
    }
    
    private static Map<Condition, BlockStateModel> createConditionMap(String blockStateJson){
        Map<Condition, BlockStateModel> conditionMap = new HashMap<>(4); //decrease initial capacity, usually only need one or two block state models
        JsonObject root = getJsonObject(blockStateJson);
        if(root.has("variants")){
            JsonObject variants = root.getAsJsonObject("variants");
            for(Entry<String, JsonElement> entry : variants.entrySet()){
                String rawConditions = entry.getKey();
                JsonObject modelJsonObj = entry.getValue().isJsonArray() ?
                                        entry.getValue().getAsJsonArray().get(0).getAsJsonObject() :
                                        entry.getValue().getAsJsonObject();
                BlockStateModel model = readModel(modelJsonObj);
                Condition condition;
                if(rawConditions.trim().isEmpty()){
                    condition = (BlockState blockState) -> true;
                }else{
                    condition = parseVariantsStatement(rawConditions);
                }
                conditionMap.put(condition, model);
            }
        }else if(root.has("multipart")){
            JsonArray multipart = root.getAsJsonArray("multipart");
            for(int i = 0; i < multipart.size(); i++){
                JsonObject possibleModel = multipart.get(i).getAsJsonObject();
                JsonObject modelJsonObj = possibleModel.get("apply").isJsonArray() ?
                                        possibleModel.get("apply").getAsJsonArray().get(0).getAsJsonObject() :
                                        possibleModel.get("apply").getAsJsonObject();
                BlockStateModel model = readModel(modelJsonObj);
                Condition condition;
                if(!possibleModel.has("when")){
                    condition = (BlockState blockState) -> true;
                }else{
                    JsonObject whenStatement = possibleModel.getAsJsonObject("when");
                    condition = parseWhenStatement(whenStatement);
                }
                conditionMap.put(condition, model);
            }
        }else{
            Logger.warning("Block State Json contained neither 'variants' nor 'multipart'.");
            throw new RuntimeException("Bad Block State Json. Json does not contain 'variants' or 'multipart'.");
        }
        return conditionMap;
    }
    
    private static Condition parseWhenStatement(JsonObject whenJson){
        if(whenJson.has("OR")){
            JsonArray orJson = whenJson.getAsJsonArray("OR");
            List<Condition> subConditions = new ArrayList<>();
            for(int i = 0; i < orJson.size(); i++){
                subConditions.add(parseWhenStatement(orJson.get(i).getAsJsonObject()));
            }
            Condition condition = (BlockState blockState) -> {
                for(Condition subCondition : subConditions){
                    if(subCondition.checkCondition(blockState)){
                        return true;
                    }
                }
                return false;
            };
            return condition;
        }else{
            String[] checkVars = new String[whenJson.size()];
            String[] checkValues = new String[whenJson.size()];
            int i = 0;
            for(Entry<String, JsonElement> entry : whenJson.entrySet()){
                checkVars[i] = entry.getKey();
                checkValues[i] = entry.getValue().getAsString();
                i++;
            }
            Condition condition = getVariableCheckCondition(checkVars, checkValues);
            return condition;
        }
    }
    
    private static Condition parseVariantsStatement(String rawConditions){
        String[] checks = rawConditions.split(",");
        String[] checkVars = new String[checks.length];
        String[] checkValues = new String[checks.length];
        for(int i = 0; i < checks.length; i++){
            String[] checkStr = checks[i].split("=");
            checkVars[i] = checkStr[0];
            checkValues[i] = checkStr[1];
        }
        Condition condition = getVariableCheckCondition(checkVars, checkValues);
        return condition;
    }
    
    /**
     * Get a condition in which a block state must contain all the checkVars
     * as properties, and each property must have a value that matches the checkValues
     * of the same index in order to be true. Properties are compared to checkValues through toString()
     * on the property's value.
     * @param checkVars
     * @param checkValues
     * @return 
     */
    private static Condition getVariableCheckCondition(String[] checkVars, String[] checkValues){
        Condition condition = (BlockState blockState) -> {
            for(int i = 0; i < checkVars.length; i++){
                if(!blockState.hasProperty(checkVars[i])){
                    return false;
                }
                String propValue = blockState.getProperty(checkVars[i]).toString();
                boolean matchesAny = false;
                for(String value : checkValues[i].split("\\|")){
                    if(propValue.equals(value)){
                        matchesAny = true;
                    }
                }
                if(!matchesAny){
                    return false;
                }
            }
            return true;
        };
        return condition;
    }
    
    /**
     * Read a BlockModel object from a JsonObject with fields: "model", "x"(optional), and "y"(optional)
     * @param modelJson
     * @return 
     */
    private static BlockStateModel readModel(JsonObject modelJson){
        String modelName = modelJson.get("model").getAsString();
        BlockModel blockModel = BlockModelLookup.getBlockModel(modelName);
        Vector3f rotation = new Vector3f();
        if(modelJson.has("x")){
            float xRot = modelJson.get("x").getAsFloat();
            rotation.x = xRot;
        }
        if(modelJson.has("y")){
            float yRot = modelJson.get("y").getAsFloat();
            rotation.y = yRot;
        }
        return new BlockStateModel(blockModel, rotation);
    }
    
    private static JsonObject getJsonObject(String json){
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
        return root;
    }
    
    private interface Condition {
        public boolean checkCondition(BlockState blockState);
    }
    
}
