
package atomicedit.jarreading.blockstates;

import atomicedit.backend.BlockState;
import atomicedit.jarreading.blockmodels.BlockModelData;
import atomicedit.jarreading.blockmodels.GlobalBlockModelDataLookup;
import atomicedit.logging.Logger;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Justin Bonner
 */
public class BlockStateData {
    
    private List<BlockModelData> blockModelDatas;
    
    
    
    public BlockStateData(BlockState blockState, List<BlockStateDataPrecursor> precursors){
        List<BlockModelData> blockModels = new ArrayList<>();
        for(BlockStateDataPrecursor stateDataPrecursor : precursors){
            int matchScore = stateDataPrecursor.getPropertyMatcher().matchScore(blockState.blockStateProperties);
            if(matchScore >= 0){
                String modelName = stateDataPrecursor.getModelName();
                if(modelName == null){
                    Logger.warning("Null model name in Block State Data Creation");
                }
                BlockModelData blockModel = GlobalBlockModelDataLookup.createBlockModelData(modelName, stateDataPrecursor.getRotation());
                if(blockModel == null){
                    Logger.warning("Block model lookup found null for model: " + modelName);
                }
                blockModels.add(blockModel);
            }
        }
        this.blockModelDatas = blockModels;
    }

    public List<BlockModelData> getBlockModelDatas(){
        return this.blockModelDatas;
    }
    
    
}
