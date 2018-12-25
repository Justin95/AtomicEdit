
package atomicedit.jarreading.blockstates;

import atomicedit.backend.BlockState;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Justin Bonner
 */
public class GlobalBlockStateDataLookup {
    
    private static Map<String, List<BlockStateDataPrecursor>> blockNameToBlockStateDataPossibilities;
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
        List<BlockStateDataPrecursor> possibilities = blockNameToBlockStateDataPossibilities.get(blockState.name);
        BlockStateData blockStateData = new BlockStateData(blockState, possibilities);
        blockStateToBlockStateDataMapping.put(blockState, blockStateData);
        return blockStateToBlockStateDataMapping.get(blockState);
    }
    
}
