
package atomicedit.jarreading.blockstates;

import atomicedit.backend.BlockStateProperty;
import com.google.gson.JsonArray;
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
    
    
    public static List<BlockStateData> parseJson(String json){
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
        List<BlockStateData> blockStateDatas = new ArrayList<>();
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
                blockStateDatas.add(new BlockStateData(new BlockStatePropertyMatcher(properties), modelName, new Vector3f(xRot, yRot, zRot)));
            });
        }else if(root.has("multipart")){
            JsonArray multipart = root.getAsJsonArray("multipart");
            JsonObject entryData = multipart.get(0).getAsJsonObject();
            String modelName;
            if(entryData.get("apply").isJsonObject()){
                modelName = entryData.get("apply").getAsJsonObject().get("model").getAsString();
            }else{
                modelName = entryData.get("apply").getAsJsonArray().get(0).getAsJsonObject().get("model").getAsString();
            }
            float xRot = entryData.has("x") ? entryData.get("x").getAsFloat() : 0;
            float yRot = entryData.has("y") ? entryData.get("y").getAsFloat() : 0; //not sure if these are ever not integers but just in case
            float zRot = entryData.has("z") ? entryData.get("z").getAsFloat() : 0;
            blockStateDatas.add(new BlockStateData(new BlockStatePropertyMatcher(new ArrayList<>()), modelName, new Vector3f(xRot, yRot, zRot)));
            //only read whatever the first entry is to save programmer time, if desired later read all fields the right way and let block state datas have multiple models
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
    
}
