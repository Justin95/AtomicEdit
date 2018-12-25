
package atomicedit.jarreading.blockstates;

import org.joml.Vector3f;

/**
 *
 * @author Justin Bonner
 */
public class BlockStateDataPrecursor {
    
    private final BlockStatePropertyMatcher propertyMatcher;
    private final String modelName;
    private final Vector3f rotation;
    
    
    
    public BlockStateDataPrecursor(BlockStatePropertyMatcher propertyMatcher, String modelName, Vector3f rotation){
        this.propertyMatcher = propertyMatcher;
        this.modelName = modelName;
        this.rotation = rotation;
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
    
    @Override
    public String toString(){
        return "BlockStateDataPrecursor:{modelName: " + modelName + "rotation:"+ rotation+" propMatcher:" + propertyMatcher + "}";
    }
}
