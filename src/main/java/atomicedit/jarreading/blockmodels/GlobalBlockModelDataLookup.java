
package atomicedit.jarreading.blockmodels;

import atomicedit.logging.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joml.Vector3f;

/**
 *
 * @author Justin Bonner
 */
public class GlobalBlockModelDataLookup {
    
    private static Map<String, BlockModelDataPrecursor> blockModelNameToBlockModelPrecursors;
    private static final String PARAM_IDENTIFIER = "#";
    
    private static boolean initialized = false;
    
    public static void initialize(){
        if(initialized) return;
        initialized = true;
        Map<String, String> blockModelToJson = BlockModelJsonLoader.loadBlockModelJsonMap();
        blockModelNameToBlockModelPrecursors = createPrecursors(blockModelToJson);
    }
    
    private static Map<String, BlockModelDataPrecursor> createPrecursors(Map<String, String> blockModelToJson){
        Map<String, BlockModelDataPrecursor> precursors = new HashMap<>();
        for(String modelName : blockModelToJson.keySet()){
            Logger.info("Parsing model file: " + modelName);
            precursors.put(modelName, BlockModelDataParser.parseJson(blockModelToJson.get(modelName)));
        }
        precursors.putAll(HardcodedBlockModelPrecursors.HARDCODED_BLOCK_MODEL_PRECURSORS);
        return precursors;
    }
    
    public static BlockModelData createBlockModelData(String blockModelName, Vector3f blockStateRotation){
        if(!initialized){
            initialize();
        }
        Logger.info("Creating block model from parsed model data: " + blockModelName);
        BlockModelDataPrecursor modelPrecursor = blockModelNameToBlockModelPrecursors.get(blockModelName);
        return createBlockModelData(modelPrecursor, blockStateRotation);
    }
    
    private static BlockModelData createBlockModelData(BlockModelDataPrecursor precursor, Vector3f blockStateRotation){
        List<TexturedBoxPrecursor> boxPrecursors = precursor.getBoxPrecursors();
        List<TexturedBox> boxes = new ArrayList<>();
        Map<String, BlockModelDataParameter> params = precursor.getParams();
        BlockModelDataPrecursor parentPrecursor = blockModelNameToBlockModelPrecursors.get(precursor.getParentName());
        boolean isFullBlock = precursor.getIsFullBlock();
        while(parentPrecursor != null){
            for(TexturedBoxPrecursor boxPrecursor : parentPrecursor.getBoxPrecursors()){
                boxPrecursors.add(boxPrecursor.copy());
            }
            params.putAll(parentPrecursor.getParams());
            isFullBlock = isFullBlock || parentPrecursor.getIsFullBlock();
            parentPrecursor = parentPrecursor.getParentName() != null ? blockModelNameToBlockModelPrecursors.get(parentPrecursor.getParentName()) : null;
        }

        for(TexturedBoxPrecursor boxPrecursor : boxPrecursors){
            updateBoxTextureNames(params, boxPrecursor);
            /*if(boxPrecursor.getTextureNames().stream().anyMatch((texName) -> texName != null && texName.startsWith(PARAM_IDENTIFIER))){
                Logger.info("Block model is template: " + modelName); //if any of box precursor's textures are parameters that need to be looked up
            }*/
            boxes.add(new TexturedBox(boxPrecursor, blockStateRotation));
        }
        return new BlockModelData(boxes, isFullBlock);
    }
    
    private static void updateBoxTextureNames(Map<String, BlockModelDataParameter> params, TexturedBoxPrecursor boxPrecursor){
        for(CubeFace face : boxPrecursor.faces.keySet()){
            TexturedFacePrecursor facePrecursor = boxPrecursor.faces.get(face);
            if(facePrecursor.textureName.startsWith(PARAM_IDENTIFIER)){
                facePrecursor.textureName = (String) getParam(params, facePrecursor.textureName.substring(1), 0).value; //strip leading '#' and get parameter
            }
        }
    }
    
    
    private static BlockModelDataParameter getParam(Map<String, BlockModelDataParameter> params, String paramName, int depth){
        if(depth >= 15){
            Logger.warning("Too deep into parameter lookups: " + depth);
        }
        if(params.containsKey(paramName)){
            BlockModelDataParameter param = params.get(paramName);
            if(param.type == BlockModelDataParameter.ParameterType.STRING && ((String)param.value).startsWith(PARAM_IDENTIFIER) && depth < 15){
                String lookupName = ((String)param.value).substring(1);
                BlockModelDataParameter result = getParam(params, lookupName, depth + 1);
                return result != null ? result : new BlockModelDataParameter(PARAM_IDENTIFIER + paramName);
            }
            return params.get(paramName);
        }else{
            return new BlockModelDataParameter(PARAM_IDENTIFIER + paramName);
        }
    }
    
}
