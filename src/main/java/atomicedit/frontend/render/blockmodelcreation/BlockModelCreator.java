
package atomicedit.frontend.render.blockmodelcreation;

import atomicedit.AtomicEdit;
import atomicedit.settings.AtomicEditSettings;
import java.util.List;

/**
 *
 * @author Justin Bonner
 */
public class BlockModelCreator {
    
    private final BlockModelCreatorLogic modelCreatorLogic;
    
    private BlockModelCreator(BlockModelCreatorLogic logic){
        this.modelCreatorLogic = logic;
    }
    
    public static BlockModelCreator getInstance(){
        BlockModelCreatorLogic logic = (BlockModelCreatorLogic)AtomicEdit.getSettings().getSettingValueAsClassInstance(AtomicEditSettings.BLOCK_MODEL_CREATOR, BlockModelCreatorLogic.class);
        BlockModelCreator blockModelCreator = new BlockModelCreator(logic);
        return blockModelCreator;
    }
    
    public void addBlockRenderData(int x, int y, int z, ChunkSectionPlus section, List<Float> vertexData, List<Short> indicies){
        modelCreatorLogic.addBlockRenderData(x, y, z, section, vertexData, indicies);
    }
    
}
