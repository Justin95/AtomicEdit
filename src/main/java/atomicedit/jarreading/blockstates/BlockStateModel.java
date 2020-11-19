
package atomicedit.jarreading.blockstates;

import atomicedit.jarreading.blockmodels.BlockModel;
import org.joml.Vector3f;

/**
 *
 * @author Justin Bonner
 */
public class BlockStateModel {
    
    private final BlockModel blockModel;
    private final Vector3f rotation;
    private final boolean isFullBlock;
    private final boolean isOpaque;
    
    public BlockStateModel(BlockModel blockModel, Vector3f rotation){
        this.blockModel = blockModel;
        this.rotation = rotation;
        this.isFullBlock = blockModel.isFullBlock();
        this.isOpaque = blockModel.isOpaque();
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
    
    public boolean isOpaque() {
        return this.isOpaque;
    }
    
}
