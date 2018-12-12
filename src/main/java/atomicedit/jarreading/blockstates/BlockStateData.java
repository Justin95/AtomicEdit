
package atomicedit.jarreading.blockstates;

import atomicedit.jarreading.blockmodels.BlockModelData;


/**
 *
 * @author Justin Bonner
 */
public class BlockStateData {
    
    private final BlockStatePropertyMatcher propertyMatcher;
    private final String modelName;
    private BlockModelData blockModelData;
    private final int xRot;
    private final int yRot;
    private final int zRot;
    
    
    public BlockStateData(BlockStatePropertyMatcher propertyMatcher, String modelName, int xRot, int yRot, int zRot){
        this.propertyMatcher = propertyMatcher;
        this.modelName = modelName;
        this.xRot = xRot;
        this.yRot = yRot;
        this.zRot = zRot;
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

    public int getXRot() {
        return xRot;
    }

    public int getYRot() {
        return yRot;
    }

    public int getZRot() {
        return zRot;
    }
    
    
}
