
package atomicedit.jarreading.blockstates;

import atomicedit.backend.BlockStateProperty;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3f;

/**
 * Parse minecraft's block state jsons.
 * @author Justin Bonner
 */
public class BlockStateDataParser {
    
    
    public static List<BlockStateDataPrecursor> parseJson(String json){
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
        List<BlockStateDataPrecursor> blockStateDatas = new ArrayList<>();
        if(root.has("variants")){
            JsonObject variants = root.getAsJsonObject("variants");
            variants.entrySet().forEach(entry -> {
                List<BlockStateProperty> properties = parsePropertiesString(entry.getKey());
                JsonObject entryData;
                if(entry.getValue().isJsonObject()){
                    entryData = entry.getValue().getAsJsonObject();
                }else{
                    entryData = entry.getValue().getAsJsonArray().get(0).getAsJsonObject();
                }
                String modelName = entryData.get("model").getAsString();
                float xRot = entryData.has("x") ? entryData.get("x").getAsFloat() : 0;
                float yRot = entryData.has("y") ? entryData.get("y").getAsFloat() : 0; //not sure if these are ever not integers but just in case
                float zRot = entryData.has("z") ? entryData.get("z").getAsFloat() : 0;
                blockStateDatas.add(new BlockStateDataPrecursor(new BlockStatePropertyMatcher(properties), modelName, new Vector3f(xRot, yRot, zRot)));
            });
        }else if(root.has("multipart")){
            JsonArray multipart = root.getAsJsonArray("multipart");
            for(int i = 0; i < multipart.size(); i++){ 
                JsonObject entryData = multipart.get(i).getAsJsonObject();
                JsonObject apply;
                if(entryData.get("apply").isJsonObject()){
                    apply = entryData.get("apply").getAsJsonObject(); //make a block state data for each combo of 'when' statements
                }else{
                    apply = entryData.get("apply").getAsJsonArray().get(0).getAsJsonObject();
                }
                String modelName = apply.get("model").getAsString();
                float xRot = apply.has("x") ? apply.get("x").getAsFloat() : 0;
                float yRot = apply.has("y") ? apply.get("y").getAsFloat() : 0; //not sure if these are ever not integers but just in case
                float zRot = apply.has("z") ? apply.get("z").getAsFloat() : 0;
                
                List<BlockStateProperty> properties = entryData.has("when") ? parseMultipartWhenStatement(entryData.getAsJsonObject("when")) : new ArrayList<>();
                
                blockStateDatas.add(new BlockStateDataPrecursor(new BlockStatePropertyMatcher(properties), modelName, new Vector3f(xRot, yRot, zRot)));
            }
        }
        return blockStateDatas;
    }
    
    /**
     * Parse a list of block state properties from a string in the form:
     * property=value,property2=value2,property3=value3
     * @param propertyString
     * @return 
     */
    private static List<BlockStateProperty> parsePropertiesString(String propertyString){
        List<BlockStateProperty> properties = new ArrayList<>();
        if(propertyString == null || propertyString.isEmpty()){
            return properties;
        }
        String[] propertyValueStrings = propertyString.split(",");
        for(String propertyValue : propertyValueStrings){
            String[] propAndValue = propertyValue.split("=");
            String propertyName = propAndValue[0];
            String value = propAndValue[1];
            properties.add(new BlockStateProperty(propertyName, value));
        }
        return properties;
    }
    
    private static List<BlockStateProperty> parseMultipartWhenStatement(JsonObject whenStatement){
        List<BlockStateProperty> properties = new ArrayList<>();
        whenStatement.entrySet().forEach((entry) ->{
            String propName = entry.getKey();
            JsonElement propValue = entry.getValue();
            if(propValue.isJsonPrimitive()){
                BlockStateProperty property = new BlockStateProperty(propName, propValue.getAsString());
                properties.add(property);
            }
        });
        return properties;
    }
    
}
