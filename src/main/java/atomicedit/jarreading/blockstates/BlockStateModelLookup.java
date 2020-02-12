
package atomicedit.jarreading.blockstates;

import atomicedit.backend.BlockState;
import atomicedit.logging.Logger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Justin Bonner
 */
public class BlockStateModelLookup {
    
    private static Map<BlockState, List<BlockStateModel>> blockStateToModels;
    private static Map<String, BlockStateModelGenerator> blockNameToModelGenerator;
    private static boolean initialized = false;
    
    public static void initialize(){
        if(initialized){
           Logger.warning("Tried to double initialize BlockStateModelLookup.");
           return; 
        }
        initialized = true;
        Map<String, String> blockNameToBlockStateJson = BlockStateJsonLoader.loadBlockStateJsons();
        blockNameToModelGenerator = createModelListGenerators(blockNameToBlockStateJson);
        blockStateToModels = new HashMap<>();
    }
    
    private static Map<String, BlockStateModelGenerator> createModelListGenerators(Map<String, String> blockNameToBlockStateJson){
        Map<String, BlockStateModelGenerator> blockNameToModelListGenerator = new HashMap<>();
        for(String blockName : blockNameToBlockStateJson.keySet()){
            String blockStateJson = blockNameToBlockStateJson.get(blockName);
            BlockStateModelGenerator modelListGenerator = BlockStateModelGenerator.getInstance(blockName, blockStateJson);
            blockNameToModelListGenerator.put(blockName, modelListGenerator);
        }
        return blockNameToModelListGenerator;
    }
    
    /**
     * Get the block state models for the given block state.
     * The result will be cached for each block state after
     * the first time it is used.
     * @param blockState
     * @return 
     */
    public static List<BlockStateModel> getBlockStateModel(BlockState blockState){
        if(!initialized){
            throw new RuntimeException("BlockStateModelLookup was not initialized when used");
        }
        List<BlockStateModel> blockStateModels = blockStateToModels.get(blockState);
        if(blockStateModels == null){
            BlockStateModelGenerator generator = blockNameToModelGenerator.get(blockState.name);
            if(generator == null){
                Logger.error("No block state model generator for block: " + blockState.name);
                generator = blockNameToModelGenerator.get(BlockState.AIR.name);
            }
            Logger.info("Creating block state models for: " + blockState);
            blockStateModels = generator.generateBlockStateModel(blockState);
            blockStateToModels.put(blockState, blockStateModels);
        }
        return blockStateModels;
    }
    
    public static void debugPrintFootprint() {
        Logger.info("Number of blockstates with stored models: " + blockStateToModels.size());
        int numModels = 0;
        for (Entry<BlockState, List<BlockStateModel>> entry : blockStateToModels.entrySet()) {
            numModels += entry.getValue().size();
        }
        Logger.info("Number of stored blockstate models: " + numModels);
    }
    
}
