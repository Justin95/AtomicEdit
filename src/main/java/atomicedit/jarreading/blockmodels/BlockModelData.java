
package atomicedit.jarreading.blockmodels;

import java.util.List;

/**
 *
 * @author Justin Bonner
 */
public class BlockModelData {
    
    private List<TexturedBox> boxes;
    private boolean isFullBlock;
    
    public BlockModelData(List<TexturedBox> boxes, boolean isFullBlock){
        this.boxes = boxes;
        this.isFullBlock = isFullBlock;
    }
    
    public List<TexturedBox> getTexturedBoxes(){
        return this.boxes;
    }
    
    public boolean isFullBlock(){
        return this.isFullBlock;
    }
    
}
