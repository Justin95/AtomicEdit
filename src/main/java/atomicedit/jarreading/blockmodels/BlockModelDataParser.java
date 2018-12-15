
package atomicedit.jarreading.blockmodels;

import atomicedit.frontend.texture.MinecraftTexture;
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
            textures.entrySet().forEach((entry) -> params.put(entry.getKey(), new BlockModelDataParameter(entry.getKey(), entry.getValue().getAsString())));
        }
        List<TexturedBoxPrecursor> boxPrecursors = new ArrayList<>();
        boolean isFullBlock = false;
        if(root.has("elements")){
            JsonArray elements = root.getAsJsonArray("elements");
            for(JsonElement element : elements){
                TexturedBoxPrecursor box = new TexturedBoxPrecursor();
                box.smallX = element.getAsJsonObject().getAsJsonArray("from").get(0).getAsFloat();
                box.smallY = element.getAsJsonObject().getAsJsonArray("from").get(1).getAsFloat();
                box.smallZ = element.getAsJsonObject().getAsJsonArray("from").get(2).getAsFloat();
                box.largeX = element.getAsJsonObject().getAsJsonArray("to").get(0).getAsFloat();
                box.largeY = element.getAsJsonObject().getAsJsonArray("to").get(1).getAsFloat();
                box.largeZ = element.getAsJsonObject().getAsJsonArray("to").get(2).getAsFloat();
                
                isFullBlock = isFullBlock || (box.smallX == 0 && box.smallY == 0 && box.smallZ == 0 && box.largeX == 16 && box.largeY == 16 && box.largeZ == 16);
                
                JsonObject faces = element.getAsJsonObject().getAsJsonObject("faces");
                box.yPlusTexName  = faces.has("up") ? faces.getAsJsonObject("up").get("texture").getAsString() : MinecraftTexture.UNKNOWN_TEXTURE_NAME;
                box.yMinusTexName = faces.has("down") ? faces.getAsJsonObject("down").get("texture").getAsString() : MinecraftTexture.UNKNOWN_TEXTURE_NAME;
                box.xPlusTexName = faces.has("east") ? faces.getAsJsonObject("east").get("texture").getAsString() : MinecraftTexture.UNKNOWN_TEXTURE_NAME;
                box.xMinusTexName = faces.has("west") ? faces.getAsJsonObject("west").get("texture").getAsString() : MinecraftTexture.UNKNOWN_TEXTURE_NAME;
                box.zPlusTexName = faces.has("south") ? faces.getAsJsonObject("south").get("texture").getAsString() : MinecraftTexture.UNKNOWN_TEXTURE_NAME;
                box.zMinusTexName = faces.has("north") ? faces.getAsJsonObject("north").get("texture").getAsString() : MinecraftTexture.UNKNOWN_TEXTURE_NAME;
                
                //add UV and rotation if I care later TODO
                boxPrecursors.add(box);
            }
        }
        BlockModelDataPrecursor modelPrecursor = new BlockModelDataPrecursor(parentName, params, boxPrecursors, isFullBlock);
        return modelPrecursor;
    }
    
}
