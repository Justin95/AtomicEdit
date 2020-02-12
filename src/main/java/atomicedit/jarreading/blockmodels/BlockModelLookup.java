
package atomicedit.jarreading.blockmodels;

import atomicedit.logging.Logger;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Justin Bonner
 */
public class BlockModelLookup {
    
    private static Map<String, BlockModel> blockModelMap;
    private static boolean initialized = false;
    
    
    public static BlockModel getBlockModel(String modelName){
        if(!initialized){
            Logger.error("Tried to use Block Model Lookup before initialization.");
            throw new RuntimeException("Block Model Lookup was not initialized.");
        }
        return blockModelMap.get(modelName);
    }
    
    
    public static void initialize(){
        if(initialized){
            return;
        }
        initialized = true;
        Map<String, String> modelNameToJson = BlockModelJsonLoader.loadBlockModelJsonMap();
        Map<String, BlockModelPrecursor> precursorMap = BlockModelParser.parseJsons(modelNameToJson);
        HardcodedBlockModels.addBlockModelPrecursors(modelNameToJson, precursorMap);
        updateTextureNamesFromParams(precursorMap);
        blockModelMap = createBlockModels(precursorMap);
    }
    
    private static Map<String, BlockModel> createBlockModels(Map<String, BlockModelPrecursor> precursorMap){
        Map<String, BlockModel> blockModels = new HashMap<>();
        for(String modelName : precursorMap.keySet()){
            Logger.info("Creating block model: " + modelName);
            BlockModel model = BlockModel.getInstance(precursorMap.get(modelName));
            blockModels.put(modelName, model);
        }
        return blockModels;
    }
    
    /**
     * Go through each ModelBoxPrecursor and update each texture name variable with the actual texture name.
     * The json parser leaves the texture variable name.
     * @param precursorMap 
     */
    private static void updateTextureNamesFromParams(Map<String, BlockModelPrecursor> precursorMap){
        for(String modelName : precursorMap.keySet()){
            BlockModelPrecursor precursor = precursorMap.get(modelName);
            for(ModelBoxPrecursor boxPrecursor : precursor.boxes){
                for(ModelBox.ModelBoxFace face : ModelBox.ModelBoxFace.values()){
                    if(!boxPrecursor.faceExists.get(face)){
                        continue;
                    }
                    String texVar = boxPrecursor.faceToTexName.get(face);
                    String texName = precursor.lookupTextureParam(texVar);
                    boxPrecursor.faceToTexName.put(face, texName);
                }
            }
            
        }
    }
    
}
