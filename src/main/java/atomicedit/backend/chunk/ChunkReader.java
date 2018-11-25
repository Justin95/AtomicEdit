
package atomicedit.backend.chunk;

import atomicedit.backend.BlockCoord;
import atomicedit.backend.blockentity.BlockEntity;
import atomicedit.backend.BlockType;
import atomicedit.backend.entity.Entity;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtTag;
import java.util.List;

/**
 *
 * @author Justin Bonner
 */
public interface ChunkReader {
    
    public abstract ChunkCoord getChunkCoord() throws MalformedNbtTagException;
    
    public abstract int getBiomeAt(BlockCoord coord) throws MalformedNbtTagException;
    
    public abstract BlockType getBlockAt(BlockCoord coord) throws MalformedNbtTagException;
    
    public abstract short[] getBlocks(int subChunkIndex) throws MalformedNbtTagException;
    
    public abstract List<Entity> getEntities() throws MalformedNbtTagException; //read only list
    
    public abstract List<BlockEntity> getBlockEntities() throws MalformedNbtTagException; //read only list
    
    public abstract NbtTag getChunkAsNbtTag();
    
    
}
