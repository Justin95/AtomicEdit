
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
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 *
 * @author Justin Bonner
 */
public class BlockModelDataParser {
    
    static BlockModelDataPrecursor parseJson(String modelJson){
        JsonReader reader = new JsonReader(new StringReader(modelJson));
        reader.setLenient(true);
        JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
        
        String parentName = null;
        if(root.has("parent")){
            parentName = root.get("parent").getAsString();
        }
        Map<String, BlockModelDataParameter> params = new HashMap<>();
        if(root.has("textures")){
            JsonObject textures = root.getAsJsonObject("textures");
            textures.entrySet().forEach((entry) -> params.put(entry.getKey(), new BlockModelDataParameter(entry.getValue().getAsString())));
        }
        List<TexturedBoxPrecursor> boxPrecursors = new ArrayList<>();
        boolean isFullBlock = false;
        if(root.has("elements")){
            JsonArray elements = root.getAsJsonArray("elements");
            for(JsonElement element : elements){
                JsonObject boxJson = element.getAsJsonObject();
                TexturedBoxPrecursor box = new TexturedBoxPrecursor();
                Vector3f small = new Vector3f();
                Vector3f large = new Vector3f();
                small.x = boxJson.getAsJsonArray("from").get(0).getAsFloat();
                small.y = boxJson.getAsJsonArray("from").get(1).getAsFloat();
                small.z = boxJson.getAsJsonArray("from").get(2).getAsFloat();
                large.x = boxJson.getAsJsonArray("to").get(0).getAsFloat();
                large.y = boxJson.getAsJsonArray("to").get(1).getAsFloat();
                large.z = boxJson.getAsJsonArray("to").get(2).getAsFloat();
                
                box.smallCorner = small;
                box.largeCorner = large;
                
                isFullBlock = isFullBlock || (small.x == 0 && small.y == 0 && small.z == 0 && large.x == 16 && large.y == 16 && large.z == 16);
                
                box.useShade = boxJson.has("shade") ? boxJson.get("shade").getAsBoolean() : true; //if no shade param default to true
                
                if(boxJson.has("rotation")){
                    JsonObject rotJson = boxJson.getAsJsonObject("rotation");
                    Vector3f rotateAbout = new Vector3f();
                    rotateAbout.x = rotJson.get("origin").getAsJsonArray().get(0).getAsFloat();
                    rotateAbout.y = rotJson.get("origin").getAsJsonArray().get(1).getAsFloat();
                    rotateAbout.z = rotJson.get("origin").getAsJsonArray().get(2).getAsFloat();
                    String axis = rotJson.get("axis").getAsString();
                    float angle = rotJson.get("angle").getAsFloat();
                    Vector3f rotation = new Vector3f(0,0,0);
                    switch(axis){
                        case "x":
                            rotation.x = angle;
                            break;
                        case "y":
                            rotation.y = angle;
                            break;
                        case "z":
                            rotation.z = angle;
                            break;
                        default:
                            Logger.warning("Tried to rotate block model on invalid axis: " + axis + " ignoring block rotation");
                    }
                    box.rotateAbout = rotateAbout;
                    box.rotation = rotation;
                }
                
                JsonObject faces = boxJson.getAsJsonObject("faces");
                for(CubeFace cubeFace : CubeFace.values()){
                    if(!faces.has(cubeFace.faceName)){
                        continue;
                    }
                    JsonObject jsonFace = faces.getAsJsonObject(cubeFace.faceName);
                    FacePrecursor facePrecursor = new FacePrecursor();
                    if(jsonFace.has("texture")){
                        facePrecursor.textureName = jsonFace.get("texture").getAsString();
                    }
                    if(jsonFace.has("uv")){
                        Vector2f uvMin = new Vector2f();
                        Vector2f uvMax = new Vector2f();
                        uvMin.x = jsonFace.getAsJsonArray("uv").get(0).getAsFloat();
                        uvMin.y = jsonFace.getAsJsonArray("uv").get(1).getAsFloat();
                        uvMax.x = jsonFace.getAsJsonArray("uv").get(2).getAsFloat();
                        uvMax.y = jsonFace.getAsJsonArray("uv").get(3).getAsFloat();
                        facePrecursor.texCoordsMin = uvMin;
                        facePrecursor.texCoordsMax = uvMax;
                    }
                    if(jsonFace.has("tintindex")){
                        facePrecursor.tintIndex = jsonFace.get("tintindex").getAsInt();
                    }
                    box.faces.put(cubeFace, facePrecursor);
                }
                boxPrecursors.add(box);
            }
        }
        BlockModelDataPrecursor modelPrecursor = new BlockModelDataPrecursor(parentName, params, boxPrecursors, isFullBlock);
        return modelPrecursor;
    }
    
}
