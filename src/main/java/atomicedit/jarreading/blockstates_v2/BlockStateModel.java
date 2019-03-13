
package atomicedit.jarreading.blockstates_v2;

import atomicedit.jarreading.blockmodels_v2.BlockModel;
import org.joml.Vector3f;

/**
 *
 * @author Justin Bonner
 */
public class BlockStateModel {
    
    private final BlockModel blockModel;
    private final Vector3f rotation;
    private final boolean isFullBlock;
    
    public BlockStateModel(BlockModel blockModel, Vector3f rotation){
        this.blockModel = blockModel;
        this.rotation = rotation;
        this.isFullBlock = blockModel.isFullBlock();
    }
    
    public BlockModel getBlockModel(){
        return this.blockModel;
    }
    
    public Vector3f getRotation(){
        return this.rotation;
    }
    
    public boolean isFullBlock(){
        return this.isFullBlock;
    }
    
}
