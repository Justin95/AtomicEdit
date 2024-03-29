
package atomicedit.backend.chunk;

import atomicedit.backend.BlockCoord;
import atomicedit.backend.blockentity.BlockEntity;
import atomicedit.backend.BlockState;
import atomicedit.backend.biomes.BiomeMap;
import atomicedit.backend.entity.Entity;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtCompoundTag;
import java.util.List;

/**
 *
 * @author Justin Bonner
 */
public interface ChunkReader {
    
    public ChunkCoord getChunkCoord() throws MalformedNbtTagException;
    
    public BiomeMap getBiomeMap() throws MalformedNbtTagException;
    
    public int getBiomeAt(BlockCoord coord) throws MalformedNbtTagException;
    
    public BlockState getBlockAt(BlockCoord coord) throws MalformedNbtTagException;
    
    public int[] getBlocks(int subChunkIndex) throws MalformedNbtTagException;
    
    public ChunkSection getChunkSection(int subChunkIndex) throws MalformedNbtTagException;
    
    public List<Entity> getEntities() throws MalformedNbtTagException; //read only list
    
    public List<BlockEntity> getBlockEntities() throws MalformedNbtTagException; //read only list
    
    public byte[] getBlockLighting(int subChunkIndex) throws MalformedNbtTagException;
    
    public byte[] getSkyLighting(int subChunkIndex) throws MalformedNbtTagException;
    
    public NbtCompoundTag getChunkAsNbtTag();
    
    public boolean needsSaving();
    
    public boolean needsRedraw();
    
    public void clearNeedsRedraw();
    
}
