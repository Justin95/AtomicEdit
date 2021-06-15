
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
public class ErosionBlockProvider implements BlockProvider {
    
    private final int erodeNextToBlockId;
    private final int erodeToBlockId;
    private final WorldVolume volume;
    /**
     * A copy of the world in the volume represented by this block provider.
     * This should only be read from and not modified.
     */
    private final Schematic worldInVolume;
    private final int[] chances;
    
    
    public ErosionBlockProvider(WorldVolume volume, Schematic worldInVolume, BlockState erodeNextToBlockState, BlockState erodeToBlockState, int[] chances){
        this.erodeNextToBlockId = GlobalBlockStateMap.getBlockId(erodeNextToBlockState);
        this.erodeToBlockId = GlobalBlockStateMap.getBlockId(erodeToBlockState);
        this.volume = volume;
        this.worldInVolume = worldInVolume;
        this.chances = chances;
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
        if (blockAt != this.erodeToBlockId) {
            int adjacent = countAdjacent(blocks, x, y, z); //between [0, 6]
            return Math.random() * 100 <= chances[adjacent] ? erodeToBlockId : blockAt;
        }
        return blockAt;
    }
    
    private int countAdjacent(int[] blocks, int x, int y, int z) {
        int adjacent = 0;
        if (volume.containsXYZ(x + 1, y, z)) {
             int blocksIndex = GeneralUtils.getIndexYZX(x + 1, y, z, volume.getEnclosingBox().getXLength(), volume.getEnclosingBox().getZLength());
             if (blocks[blocksIndex] == this.erodeNextToBlockId) {
                 adjacent++;
             }
        }
        if (volume.containsXYZ(x - 1, y, z)) {
             int blocksIndex = GeneralUtils.getIndexYZX(x - 1, y, z, volume.getEnclosingBox().getXLength(), volume.getEnclosingBox().getZLength());
             if (blocks[blocksIndex] == this.erodeNextToBlockId) {
                 adjacent++;
             }
        }
        if (volume.containsXYZ(x, y + 1, z)) {
             int blocksIndex = GeneralUtils.getIndexYZX(x, y + 1, z, volume.getEnclosingBox().getXLength(), volume.getEnclosingBox().getZLength());
             if (blocks[blocksIndex] == this.erodeNextToBlockId) {
                 adjacent++;
             }
        }
        if (volume.containsXYZ(x, y - 1, z)) {
             int blocksIndex = GeneralUtils.getIndexYZX(x, y - 1, z, volume.getEnclosingBox().getXLength(), volume.getEnclosingBox().getZLength());
             if (blocks[blocksIndex] == this.erodeNextToBlockId) {
                 adjacent++;
             }
        }
        if (volume.containsXYZ(x, y, z + 1)) {
             int blocksIndex = GeneralUtils.getIndexYZX(x, y, z + 1, volume.getEnclosingBox().getXLength(), volume.getEnclosingBox().getZLength());
             if (blocks[blocksIndex] == this.erodeNextToBlockId) {
                 adjacent++;
             }
        }
        if (volume.containsXYZ(x, y, z - 1)) {
             int blocksIndex = GeneralUtils.getIndexYZX(x, y, z - 1, volume.getEnclosingBox().getXLength(), volume.getEnclosingBox().getZLength());
             if (blocks[blocksIndex] == this.erodeNextToBlockId) {
                 adjacent++;
             }
        }
        return adjacent;
    }
    
    
    
    @Override
    public void doForBlock(ActionForBlock action){
        volume.doForXyz((x, y, z, index) -> {
            action.doAction(x, y, z, getBlockAt(x, y, z));
        });
    }
    
}
