
package atomicedit.backend.chunk;

import atomicedit.backend.BlockCoord;
import atomicedit.backend.blockentity.BlockEntity;
import atomicedit.backend.BlockState;
import atomicedit.backend.entity.Entity;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtTag;
import java.util.List;

/**
 *
 * @author Justin Bonner
 */
public interface ChunkReader {
    
    public ChunkCoord getChunkCoord() throws MalformedNbtTagException;
    
    public int getBiomeAt(BlockCoord coord) throws MalformedNbtTagException;
    
    public BlockState getBlockAt(BlockCoord coord) throws MalformedNbtTagException;
    
    public short[] getBlocks(int subChunkIndex) throws MalformedNbtTagException;
    
    public ChunkSection getChunkSection(int subChunkIndex) throws MalformedNbtTagException;
    
    public List<Entity> getEntities() throws MalformedNbtTagException; //read only list
    
    public List<BlockEntity> getBlockEntities() throws MalformedNbtTagException; //read only list
    
    public NbtTag getChunkAsNbtTag();
    
    public boolean needsSaving();
    
    public boolean needsRedraw();
    
    public void clearNeedsRedraw();
    
}
