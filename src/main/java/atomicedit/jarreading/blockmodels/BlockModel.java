
package atomicedit.jarreading.blockmodels;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Justin Bonner
 */
public class BlockModel {
    
    private final List<ModelBox> modelBoxes;
    private final boolean isFullBlock;
    private final boolean isOpaque;
    
    private BlockModel(List<ModelBox> modelBoxes){
        this.modelBoxes = modelBoxes;
        boolean fullBlock = false;
        boolean opaque = false;
        for(ModelBox model : modelBoxes){
            if(model.isFullBlock()){
                fullBlock = true;
            }
            if (model.isOpaque()) {
                opaque = true;
            }
        }
        this.isFullBlock = fullBlock;
        this.isOpaque = opaque;
    }
    
    public static BlockModel getInstance(BlockModelPrecursor precursor){
        List<ModelBox> models = new ArrayList<>(precursor.boxes.size());
        for(ModelBoxPrecursor boxPrecursor : precursor.boxes){
            models.add(ModelBox.getInstance(boxPrecursor));
        }
        return new BlockModel(models);
    }
    
    public boolean isFullBlock() {
        return this.isFullBlock;
    }
    
    public boolean isOpaque() {
        return this.isOpaque;
    }
    
    public List<ModelBox> getBlockModels(){
        return this.modelBoxes;
    }
    
}
