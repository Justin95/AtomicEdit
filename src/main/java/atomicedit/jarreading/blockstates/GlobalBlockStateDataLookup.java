
package atomicedit.jarreading.blockstates;

import atomicedit.backend.BlockState;
import atomicedit.jarreading.blockmodels.BlockModelData;
import atomicedit.jarreading.blockmodels.GlobalBlockModelDataLookup;
import atomicedit.logging.Logger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Justin Bonner
 */
public class GlobalBlockStateDataLookup {
    
    private static Map<String, List<BlockStateData>> blockNameToBlockStateDataPossibilities;
    private static Map<BlockState, BlockStateData> blockStateToBlockStateDataMapping = new HashMap<>();
    private static boolean loaded = false;
    
    
    public static void initialize(){
        if(loaded) return;
        loaded = true;
        blockNameToBlockStateDataPossibilities = BlockStateLoader.getBlockNameToBlockStateDataPossibilities();
    }
    
    
    public static BlockStateData getBlockStateDataFromBlockState(BlockState blockState){
        if(!loaded){
            initialize();
        }
        if(blockStateToBlockStateDataMapping.containsKey(blockState)){
            return blockStateToBlockStateDataMapping.get(blockState);
        }
        List<BlockStateData> possibilities = blockNameToBlockStateDataPossibilities.get(blockState.name);
        for(BlockStateData stateData : possibilities){
            if(stateData.getPropertyMatcher().matches(blockState.blockStateProperties)){
                //add block model to blockstate data
                BlockModelData blockModel = GlobalBlockModelDataLookup.createBlockModelData(stateData.getModelName(), stateData.getRotation());
                if(blockModel == null){
                    Logger.error("Block model lookup found null for model: " + stateData.getModelName());
                }
                stateData.setBlockModelData(blockModel);
                blockStateToBlockStateDataMapping.put(blockState, stateData);
                break;
            }
        }
        return blockStateToBlockStateDataMapping.get(blockState);
    }
    
}
