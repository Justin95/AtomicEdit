
package atomicedit.frontend.render.blockmodelcreation;

import java.util.List;

/**
 *
 * @author Justin Bonner
 */
public class BlockModelCreator {
    
    private static BlockModelCreator INSTANCE; //singleton design wont allow changing block model creator setting without restarting
    
    private final BlockModelCreatorLogic modelCreatorLogic;
    
    private BlockModelCreator(BlockModelCreatorLogic logic){
        this.modelCreatorLogic = logic;
    }
    
    public static BlockModelCreator getInstance(){
        if(INSTANCE == null){
            BlockModelCreatorLogic logic = BlockModelCreator1_13Logic.getInstance(); //TODO: this should be chosen based on chunk DataVersion nbt tag
            INSTANCE = new BlockModelCreator(logic);
        }
        return INSTANCE;
    }
    
    public void addBlockRenderData(int x, int y, int z, ChunkSectionPlus section, List<Float> vertexData, List<Integer> indicies, boolean includeTranslucent){
        modelCreatorLogic.addBlockRenderData(x, y, z, section, vertexData, indicies, includeTranslucent);
    }
    
}
