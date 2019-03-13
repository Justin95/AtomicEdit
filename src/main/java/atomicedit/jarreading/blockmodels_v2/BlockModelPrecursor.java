
package atomicedit.jarreading.blockmodels_v2;

import atomicedit.logging.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Justin Bonner
 */
public class BlockModelPrecursor {
    
    Map<String, String> textureParams;
    List<ModelBoxPrecursor> boxes;
    
    BlockModelPrecursor(){
        textureParams = new HashMap<>();
        boxes = new ArrayList<>();
    }
    
    String lookupTextureParam(String texParam){
        if(!texParam.startsWith("#")){
            Logger.warning("Tried to look up a texture parameter that does not start with '#': " + texParam);
            throw new IllegalArgumentException("Looked up bad tex parameter");
        }
        return lookupTexParam(texParam, 0);
    }
    
    private String lookupTexParam(String texParam, int depth){
        texParam = texParam.substring(1);
        String result = textureParams.get(texParam);
        if(result == null){
            return null;
        }
        if(result.startsWith("#")){
            return lookupTexParam(result, depth + 1);
        }
        return result;
    }
    
}
