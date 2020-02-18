
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
    
    private BlockModel(List<ModelBox> modelBoxes){
        this.modelBoxes = modelBoxes;
        boolean fullBlock = false;
        for(ModelBox model : modelBoxes){
            if(model.isFullBlock()){
                fullBlock = true;
                break;
            }
        }
        this.isFullBlock = fullBlock;
    }
    
    public static BlockModel getInstance(BlockModelPrecursor precursor){
        List<ModelBox> models = new ArrayList<>(precursor.boxes.size());
        for(ModelBoxPrecursor boxPrecursor : precursor.boxes){
            models.add(ModelBox.getInstance(boxPrecursor));
        }
        return new BlockModel(models);
    }
    
    public boolean isFullBlock(){
        return this.isFullBlock;
    }
    
    public List<ModelBox> getBlockModels(){
        return this.modelBoxes;
    }
    
}
