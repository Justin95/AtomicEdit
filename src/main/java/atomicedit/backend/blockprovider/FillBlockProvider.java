
package atomicedit.backend.blockprovider;

import atomicedit.backend.BlockState;
import atomicedit.backend.GlobalBlockStateMap;
import atomicedit.volumes.Volume;

/**
 *
 * @author Justin Bonner
 */
public class FillBlockProvider implements BlockProvider{
    
    
    private short blockId;
    private Volume volume;
    
    
    public FillBlockProvider(Volume volume, BlockState blockType){
        this.blockId = GlobalBlockStateMap.getBlockId(blockType);
        this.volume = volume;
    }

    @Override
    public Volume getVolume() {
        return this.volume;
    }

    @Override
    public short getBlockAt(int x, int y, int z) {
        return this.blockId;
    }
    
    @Override
    public void doForBlock(ActionForBlock action){
        volume.doForXyz((x, y, z) -> {
            action.doAction(x, y, z, blockId);
        });
    }
    
}
