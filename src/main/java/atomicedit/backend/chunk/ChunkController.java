
package atomicedit.backend.chunk;

import atomicedit.backend.BlockCoord;
import atomicedit.backend.BlockState;
import atomicedit.backend.biomes.BiomeMap;
import atomicedit.backend.entity.Entity;
import atomicedit.backend.blockentity.BlockEntity;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtCompoundTag;

/**
 *
 * @author Justin Bonner
 */
public abstract class ChunkController implements ChunkReader {
    
    protected Chunk chunk;
    
    public ChunkController(Chunk chunk){
        this.chunk = chunk;
    }
    
    public Chunk getChunk(){
        flushCacheToChunkNbt();
        return this.chunk;
    }
    
    
    public abstract void setBiomeMap(BiomeMap biomeMap) throws MalformedNbtTagException;
    
    public abstract void setBlockAt(BlockCoord coord, BlockState block) throws MalformedNbtTagException;
    
    public abstract void setBlocks(int subChunkIndex, short[] blocks) throws MalformedNbtTagException;
    
    public abstract void addEntity(Entity entity) throws MalformedNbtTagException;
    
    public abstract void removeEntity(Entity entity) throws MalformedNbtTagException;
    
    public abstract void addBlockEntity(BlockEntity tileEntity) throws MalformedNbtTagException;
    
    public abstract void removeBlockEntity(BlockEntity tileEntity) throws MalformedNbtTagException;
    
    public abstract void setBlockLighting(int subChunkIndex, byte[] blockLighting) throws MalformedNbtTagException;
    
    public abstract void setSkyLighting(int subChunkIndex, byte[] skyLighting) throws MalformedNbtTagException;
    
    public abstract void setChunkNbtTag(NbtCompoundTag tag) throws MalformedNbtTagException; //call this after editing the NbtTag from getChunkAsNbtTag
    
    public abstract void flushCacheToChunkNbt();
    
    /**
     * If this chunk data version supports cubic biomes.
     * @return 
     */
    protected abstract boolean useCubicBiomes();
    
    /**
     * The number of chunk sections high this chunk data version calls for.
     * @return 
     */
    public abstract int chunkHeightInSections();
    
    protected void declareNbtChanged(){
        this.chunk.setNeedsSaving(true);
    }
    
    protected void declareVisiblyChanged(){
        this.chunk.setNeedsLightingCalc(true);
        this.chunk.setNeedsRedraw(true);
    }
    
    public abstract void declareChunkSectionCacheChanged();
    
    public void setNeedsRedraw() {
        this.chunk.setNeedsRedraw(true);
    }
    
    public boolean needsRedraw(){
        return this.chunk.needsRedraw();
    }
    
    public boolean needsSaving(){
        return this.chunk.needsSaving();
    }
    
    public boolean needsLightingCalc(){
        return this.chunk.needsLightingCalc();
    }
    
    public void clearNeedsLightingCalc() {
        this.chunk.setNeedsLightingCalc(false);
    }
    
    public void clearNeedsRedraw(){
        this.chunk.setNeedsRedraw(false);
    }
    
}
