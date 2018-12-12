
package atomicedit.jarreading.blockmodels;

import atomicedit.logging.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Justin Bonner
 */
public class GlobalBlockModelDataLookup {
    
    private static Map<String, BlockModelData> blockModelNameToBlockModel;
    private static String PARAM_IDENTIFIER = "#";
    
    private static boolean initialized = false;
    
    public static void initialize(){
        if(initialized) return;
        initialized = true;
        Map<String, String> blockModelToJson = BlockModelJsonLoader.loadBlockModelJsonMap();
        Map<String, BlockModelDataPrecursor> blockModelPrecursors = createPrecursors(blockModelToJson);
        blockModelNameToBlockModel = createBlockModelData(blockModelPrecursors);
    }
    
    public static BlockModelData getBlockModelData(String modelName){
        if(!initialized){
            initialize();
        }
        return blockModelNameToBlockModel.get(modelName);
    }
    
    private static Map<String, BlockModelDataPrecursor> createPrecursors(Map<String, String> blockModelToJson){
        Map<String, BlockModelDataPrecursor> precursors = new HashMap<>();
        for(String modelName : blockModelToJson.keySet()){
            Logger.info("Parsing model file: " + modelName);
            precursors.put(modelName, BlockModelDataParser.parseJson(blockModelToJson.get(modelName)));
        }
        return precursors;
    }
    
    private static Map<String, BlockModelData> createBlockModelData(Map<String, BlockModelDataPrecursor> blockModelPrecursors){
        Map<String, BlockModelData> blockModelMap = new HashMap<>();
        for(String modelName : blockModelPrecursors.keySet()){
            Logger.info("Creating block model from parsed model data: " + modelName);
            BlockModelDataPrecursor precursor = blockModelPrecursors.get(modelName);
            List<TexturedBoxPrecursor> boxPrecursors = precursor.getBoxPrecursors();
            List<TexturedBox> boxes = new ArrayList<>();
            Map<String, BlockModelDataParameter> params = precursor.getParams();
            BlockModelDataPrecursor parentPrecursor = blockModelPrecursors.get(precursor.getParentName());
            boolean isFullBlock = precursor.getIsFullBlock();
            while(parentPrecursor != null){
                for(TexturedBoxPrecursor boxPrecursor : parentPrecursor.getBoxPrecursors()){
                    boxPrecursors.add(boxPrecursor.copy());
                }
                params.putAll(parentPrecursor.getParams());
                isFullBlock = isFullBlock || parentPrecursor.getIsFullBlock();
                parentPrecursor = parentPrecursor.getParentName() != null ? blockModelPrecursors.get(parentPrecursor.getParentName()) : null;
            }
            
            for(TexturedBoxPrecursor boxPrecursor : boxPrecursors){
                updateBoxTextureNames(params, boxPrecursor);
                if(boxPrecursor.getTextureNames().stream().anyMatch((texName) -> texName != null && texName.startsWith(PARAM_IDENTIFIER))){
                    Logger.info("Block model is template: " + modelName); //if any of box precursor's textures are parameters that need to be looked up
                }
                boxes.add(new TexturedBox(boxPrecursor));
            }
            blockModelMap.put(modelName, new BlockModelData(boxes, isFullBlock));
        }
        return blockModelMap;
    }
    
    private static void updateBoxTextureNames(Map<String, BlockModelDataParameter> params, TexturedBoxPrecursor boxPrecursor){
        if(boxPrecursor.xMinusTexName.startsWith(PARAM_IDENTIFIER)){
            boxPrecursor.xMinusTexName = (String) getParam(params, boxPrecursor.xMinusTexName.substring(1), 0).value; //strip leading '#' and get parameter
        }
        if(boxPrecursor.xPlusTexName.startsWith(PARAM_IDENTIFIER)){
            boxPrecursor.xPlusTexName = (String) getParam(params, boxPrecursor.xPlusTexName.substring(1), 0).value;
        }
        if(boxPrecursor.yMinusTexName.startsWith(PARAM_IDENTIFIER)){
            boxPrecursor.yMinusTexName = (String) getParam(params, boxPrecursor.yMinusTexName.substring(1), 0).value;
        }
        if(boxPrecursor.yPlusTexName.startsWith(PARAM_IDENTIFIER)){
            boxPrecursor.yPlusTexName = (String) getParam(params, boxPrecursor.yPlusTexName.substring(1), 0).value;
        }
        if(boxPrecursor.zMinusTexName.startsWith(PARAM_IDENTIFIER)){
            boxPrecursor.zMinusTexName = (String) getParam(params, boxPrecursor.zMinusTexName.substring(1), 0).value;
        }
        if(boxPrecursor.zPlusTexName.startsWith(PARAM_IDENTIFIER)){
            boxPrecursor.zPlusTexName = (String) getParam(params, boxPrecursor.zPlusTexName.substring(1), 0).value;
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
                return result != null ? result : new BlockModelDataParameter("", PARAM_IDENTIFIER + paramName);
            }
            return params.get(paramName);
        }else{
            return new BlockModelDataParameter("", PARAM_IDENTIFIER + paramName);
        }
    }
    
}
