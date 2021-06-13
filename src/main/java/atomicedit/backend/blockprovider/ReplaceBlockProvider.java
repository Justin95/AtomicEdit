
package atomicedit.backend.blockprovider;

import atomicedit.backend.BlockState;
import atomicedit.backend.GlobalBlockStateMap;
import atomicedit.backend.schematic.Schematic;
import atomicedit.backend.utils.GeneralUtils;
import atomicedit.volumes.Volume;
import atomicedit.volumes.WorldVolume;

/**
 *
 * @author Justin Bonner
 */
public class ReplaceBlockProvider implements BlockProvider {
    
    private final int fromBlockId;
    private final int toBlockId;
    private final WorldVolume volume;
    /**
     * A copy of the world in the volume represented by this block provider.
     * This should only be read from and not modified.
     */
    private final Schematic worldInVolume;
    
    
    public ReplaceBlockProvider(WorldVolume volume, Schematic worldInVolume, BlockState fromBlockState, BlockState toBlockState){
        this.fromBlockId = GlobalBlockStateMap.getBlockId(fromBlockState);
        this.toBlockId = GlobalBlockStateMap.getBlockId(toBlockState);
        this.volume = volume;
        this.worldInVolume = worldInVolume;
    }

    @Override
    public Volume getVolume() {
        return this.volume;
    }

    @Override
    public int getBlockAt(int x, int y, int z) {
        int[] blocks = worldInVolume.getBlocks();
        int blocksIndex = GeneralUtils.getIndexYZX(x, y, z, volume.getEnclosingBox().getXLength(), volume.getEnclosingBox().getZLength());
        int blockAt = blocks[blocksIndex];
        return blockAt == fromBlockId ? toBlockId : blockAt;
    }
    
    @Override
    public void doForBlock(ActionForBlock action){
        volume.doForXyz((x, y, z) -> {
            action.doAction(x, y, z, getBlockAt(x, y, z));
        });
    }
    
}
