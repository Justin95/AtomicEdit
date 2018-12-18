
package atomicedit.jarreading.blockstates;

import atomicedit.jarreading.blockmodels.BlockModelData;
import org.joml.Vector3f;


/**
 *
 * @author Justin Bonner
 */
public class BlockStateData {
    
    private final BlockStatePropertyMatcher propertyMatcher;
    private final String modelName;
    private BlockModelData blockModelData;
    private final Vector3f rotation;
    
    
    public BlockStateData(BlockStatePropertyMatcher propertyMatcher, String modelName, Vector3f rotation){
        this.propertyMatcher = propertyMatcher;
        this.modelName = modelName;
        this.rotation = rotation;
    }

    public BlockModelData getBlockModelData(){
        return this.blockModelData;
    }
    
    void setBlockModelData(BlockModelData blockModelData){
        this.blockModelData = blockModelData; //TODO copy and rotate
    }
    
    public BlockStatePropertyMatcher getPropertyMatcher() {
        return propertyMatcher;
    }

    public String getModelName() {
        return modelName;
    }

    public Vector3f getRotation() {
        return rotation;
    }
    
    
}
