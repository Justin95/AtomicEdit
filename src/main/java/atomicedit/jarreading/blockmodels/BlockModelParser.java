
package atomicedit.jarreading.blockmodels;

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
import org.joml.Vector4f;

/**
 * Spec: https://minecraft.gamepedia.com/Model
 * @author Justin Bonner
 */
public class BlockModelParser {
    
    private static final int MAX_RECURSION = 20;
    
    public static Map<String, BlockModelPrecursor> parseJsons(Map<String, String> nameToJsonMap){
        Map<String, BlockModelPrecursor> precursorMap = new HashMap<>();
        for(String modelName : nameToJsonMap.keySet()){
            JsonObject root = getJsonObject(nameToJsonMap.get(modelName));
            BlockModelPrecursor precursor = parseBlockModel(nameToJsonMap, modelName, root, 0);
            precursorMap.put(modelName, precursor);
        }
        return precursorMap;
    }
    
    public static BlockModelPrecursor parseBlockModel(Map<String, String> nameToJsonMap, String modelName){
        return parseBlockModel(nameToJsonMap, modelName, getJsonObject(nameToJsonMap.get(modelName)), 0);
    }
    
    private static BlockModelPrecursor parseBlockModel(Map<String, String> nameToJsonMap, String modelName, JsonObject root, int recursiveDepth){
        if(recursiveDepth > MAX_RECURSION){
            throw new RuntimeException("Cannot parse block models, too deeply nested");
        }
        BlockModelPrecursor precursor;
        if(root.has("parent")){
            String parentName = root.get("parent").getAsString();
            //some models are just 'block/blah' and some are 'minecraft:block/blah' in 1.16
            Logger.debug("Lookup up parent model: " + parentName);
            String precursorJsonStr;
            if (nameToJsonMap.containsKey(parentName)) {
                precursorJsonStr = nameToJsonMap.get(parentName);
            } else if (parentName.startsWith("minecraft:")) {
                String altParentName = parentName.substring("minecraft:".length());
                if (!nameToJsonMap.containsKey(altParentName)) {
                    Logger.warning("Could not find model precursor '" + altParentName + "'.");
                    throw new RuntimeException("Could not find model precursor '" + altParentName + "'.");
                }
                precursorJsonStr = nameToJsonMap.get(altParentName);
            } else if (nameToJsonMap.containsKey("minecraft:" + parentName)) {
                precursorJsonStr = nameToJsonMap.get("minecraft:" + parentName);
            } else {
                Logger.warning("Could not find model precursor '" + parentName + "'.");
                throw new RuntimeException("Could not find model precursor '" + parentName + "'.");
            }
            precursor = parseBlockModel(nameToJsonMap, parentName, getJsonObject(precursorJsonStr), recursiveDepth + 1);
        }else{
            precursor = new BlockModelPrecursor();
        }
        if(root.has("textures")){
            JsonObject textures = root.getAsJsonObject("textures");
            for(Entry<String, JsonElement> texEntry : textures.entrySet()){
                if("particle".equals(texEntry.getKey())){
                    continue; //texture vars cannot be named particle
                }
                precursor.textureParams.put(texEntry.getKey(), texEntry.getValue().getAsString());
            }
        }
        if(root.has("elements")){
            List<ModelBoxPrecursor> boxes = new ArrayList<>();
            JsonArray elements = root.getAsJsonArray("elements");
            for(int i = 0; i < elements.size(); i++){
                JsonObject element = elements.get(i).getAsJsonObject();
                ModelBoxPrecursor boxPrecursor = new ModelBoxPrecursor();
                
                //read position
                float startX = element.get("from").getAsJsonArray().get(0).getAsFloat() / 16f;
                float startY = element.get("from").getAsJsonArray().get(1).getAsFloat() / 16f;
                float startZ = element.get("from").getAsJsonArray().get(2).getAsFloat() / 16f;
                
                float endX = element.get("to").getAsJsonArray().get(0).getAsFloat() / 16f;
                float endY = element.get("to").getAsJsonArray().get(1).getAsFloat() / 16f;
                float endZ = element.get("to").getAsJsonArray().get(2).getAsFloat() / 16f;
                
                Vector3f minCorner = new Vector3f(Math.min(startX, endX), Math.min(startY, endY), Math.min(startZ, endZ));
                Vector3f maxCorner = new Vector3f(Math.max(startX, endX), Math.max(startY, endY), Math.max(startZ, endZ));
                
                boxPrecursor.minPosition = minCorner;
                boxPrecursor.maxPosition = maxCorner;
                
                //read shade
                if(element.has("shade")){
                    boxPrecursor.useShade = element.get("shade").getAsBoolean();
                }
                
                //read rotation
                if(element.has("rotation")){
                    JsonObject rotation = element.getAsJsonObject("rotation");
                    if(rotation.has("origin")){
                        float originX = rotation.get("origin").getAsJsonArray().get(0).getAsFloat() / 16f;
                        float originY = rotation.get("origin").getAsJsonArray().get(1).getAsFloat() / 16f;
                        float originZ = rotation.get("origin").getAsJsonArray().get(2).getAsFloat() / 16f;
                        Vector3f origin = new Vector3f(originX, originY, originZ);
                        boxPrecursor.rotateAbout = origin;
                    }
                    float angle = rotation.get("angle").getAsFloat();
                    switch(rotation.get("axis").getAsString()){
                        case "x":
                            boxPrecursor.rotation = new Vector3f(angle, 0, 0);
                            break;
                        case "y":
                            boxPrecursor.rotation = new Vector3f(0, angle, 0);
                            break;
                        case "z":
                            boxPrecursor.rotation = new Vector3f(0, 0, angle);
                            break;
                        default:
                            Logger.warning("Invalid block model json. Rotation axis cannot be " + rotation.get("axis").getAsString());
                            throw new IllegalArgumentException("Bad rotation axis in model json. Model: " + modelName);
                    }
                }
                
                //read faces
                if(element.has("faces")){
                    JsonObject faces = element.getAsJsonObject("faces");
                    for(BoxFace face : BoxFace.values()){
                        if(!faces.has(face.nameInJson)){
                            continue;
                        }
                        boxPrecursor.faceExists.put(face.modelFace, Boolean.TRUE);
                        JsonObject faceJson = faces.getAsJsonObject(face.nameInJson);
                        if(faceJson.has("uv")){
                            
                            //(uvIndex + defaultIndex) % 4 instead of just defaultIndex to allow texture rotation in 90 degree increments
                            float texMinX = faceJson.get("uv").getAsJsonArray().get(0).getAsFloat() / 16f;
                            float texMinY = faceJson.get("uv").getAsJsonArray().get(1).getAsFloat() / 16f;
                            float texMaxX = faceJson.get("uv").getAsJsonArray().get(2).getAsFloat() / 16f;
                            float texMaxY = faceJson.get("uv").getAsJsonArray().get(3).getAsFloat() / 16f;
                            boxPrecursor.faceToTexCoords.put(face.modelFace, new Vector4f(texMinX, texMinY, texMaxX, texMaxY));
                        }else{
                            boxPrecursor.faceToTexCoords.put(face.modelFace, createTextureCoordsFromPosition(face, minCorner, maxCorner));
                        }
                        if(faceJson.has("rotation")){
                            //rotations always in 90 degree increments
                            int numTextureTurns = (faceJson.get("rotation").getAsInt() / 90) % 4;
                            boxPrecursor.faceToNumTextureRotations.put(face.modelFace, numTextureTurns);
                        }
                        if(faceJson.has("texture")){
                            boxPrecursor.faceToTexName.put(face.modelFace, faceJson.get("texture").getAsString());
                        }
                        if(faceJson.has("tintindex")){
                            //actual value of tintindex does not matter
                            boxPrecursor.faceToBlockTintColor.put(face.modelFace, guessBlockTint(modelName));
                        }
                    }
                }
                
                
                boxes.add(boxPrecursor);
            }
            precursor.boxes = boxes;
        }
        return precursor;
    }
    
    private static Vector4f createTextureCoordsFromPosition(BoxFace face, Vector3f minCoords, Vector3f maxCoords){
        float texMinX = 0;
        float texMinY = 0;
        float texMaxX = 1;
        float texMaxY = 1;
        switch(face){
            case NORTH:
            case SOUTH:
                texMinX = minCoords.x;
                texMinY = minCoords.y;
                texMaxX = maxCoords.x;
                texMaxY = maxCoords.y;
                break;
            case EAST:
            case WEST:
                texMinX = minCoords.z;
                texMinY = minCoords.y;
                texMaxX = maxCoords.z;
                texMaxY = maxCoords.y;
                break;
            case UP:
            case DOWN:
                texMinX = minCoords.x;
                texMinY = minCoords.z;
                texMaxX = maxCoords.x;
                texMaxY = maxCoords.z;
                break;
            
        }
        return new Vector4f(texMinX, texMinY, texMaxX, texMaxY);
    }
    
    private static Vector3f guessBlockTint(String modelName){
        //this is beyond science
        if(modelName.contains("water")){
            return ModelBox.BLUE_BLOCK_TINT;
        }else if(modelName.contains("red")){
            return ModelBox.RED_BLOCK_TINT;
        }
        return ModelBox.GREEN_BLOCK_TINT;
    }
    
    private static JsonObject getJsonObject(String json){
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
        return root;
    }
    
    private static enum BoxFace{
        NORTH("north",  ModelBox.ModelBoxFace.Z_MINUS),
        SOUTH("south",  ModelBox.ModelBoxFace.Z_PLUS),
        EAST("east",    ModelBox.ModelBoxFace.X_PLUS),
        WEST("west",    ModelBox.ModelBoxFace.X_MINUS),
        UP("up",        ModelBox.ModelBoxFace.Y_PLUS),
        DOWN("down",    ModelBox.ModelBoxFace.Y_MINUS)
        ;
        
        private final String nameInJson;
        private final ModelBox.ModelBoxFace modelFace;
        
        BoxFace(String nameInJson, ModelBox.ModelBoxFace modelFace){
            this.nameInJson = nameInJson;
            this.modelFace = modelFace;
        }
        
    }
    
}
