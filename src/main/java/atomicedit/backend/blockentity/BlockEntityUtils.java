
package atomicedit.backend.blockentity;

import atomicedit.backend.BlockCoord;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtIntTag;
import atomicedit.volumes.WorldVolume;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Justin Bonner
 */
public class BlockEntityUtils {
    
    /**
     * Translate the coordinates
     * @param toUpdate
     * @param volume
     * @return
     * @throws MalformedNbtTagException 
     */
    public static Collection<BlockEntity> translateBlockEntityCoordsToVolume(Collection<BlockEntity> toUpdate, WorldVolume volume) throws MalformedNbtTagException {
        if (toUpdate == null) {
            return null;
        }
        List<BlockEntity> updated = new ArrayList<>();
        for (BlockEntity blockEntity : toUpdate) {
            blockEntity = blockEntity.copy();
            BlockCoord coord = blockEntity.getBlockCoord();
            int x = coord.x - volume.getSmallestPoint().x;
            int y = coord.y - volume.getSmallestPoint().y;
            int z = coord.z - volume.getSmallestPoint().z;
            blockEntity.setBlockCoord(x, y, z);
            updated.add(blockEntity);
        }
        return updated;
    }

    public static Collection<BlockEntity> translateBlockEntityCoordsToWorld(Collection<BlockEntity> toUpdate, WorldVolume volume) throws MalformedNbtTagException {
        if (toUpdate == null) {
            return null;
        }
        List<BlockEntity> updated = new ArrayList<>();
        for (BlockEntity blockEntity : toUpdate) {
            blockEntity = blockEntity.copy();
            BlockCoord coord = blockEntity.getBlockCoord();
            int x = coord.x + volume.getSmallestPoint().x;
            int y = coord.y + volume.getSmallestPoint().y;
            int z = coord.z + volume.getSmallestPoint().z;
            blockEntity.setBlockCoord(x, y, z);
            updated.add(blockEntity);
        }
        return updated;
    }
    
    
    
}
