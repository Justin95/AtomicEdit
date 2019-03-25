
package atomicedit.backend.chunk;

import atomicedit.backend.BlockCoord;
import atomicedit.backend.BlockState;
import atomicedit.backend.entity.Entity;
import atomicedit.backend.blockentity.BlockEntity;
import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtTag;

/**
 *
 * @author Justin Bonner
 */
public abstract class ChunkController implements ChunkReader{
    
    protected Chunk chunk;
    
    public ChunkController(Chunk chunk){
        this.chunk = chunk;
    }
    
    public Chunk getChunk(){
        flushCacheToChunkNbt();
        return this.chunk;
    }
    
    
    public abstract void setBiomeAt(BlockCoord coord, int biome) throws MalformedNbtTagException;
    
    public abstract void setBlockAt(BlockCoord coord, BlockState block) throws MalformedNbtTagException;
    
    public abstract void setBlocks(int subChunkIndex, short[] blocks) throws MalformedNbtTagException;
    
    public abstract void addEntity(Entity entity) throws MalformedNbtTagException;
    
    public abstract void removeEntity(Entity entity) throws MalformedNbtTagException;
    
    public abstract void addBlockEntity(BlockEntity tileEntity) throws MalformedNbtTagException;
    
    public abstract void removeBlockEntity(BlockEntity tileEntity) throws MalformedNbtTagException;;
    
    public abstract void setChunkNbtTag(NbtTag tag) throws MalformedNbtTagException; //call this after editing the NbtTag from getChunkAsNbtTag
    
    protected abstract void flushCacheToChunkNbt();
    
    protected void declareNbtChanged(){
        this.chunk.setNeedsSaving(true);
    }
    
    protected void declareVisiblyChanged(){
        this.chunk.setNeedsLightingCalc(true);
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
    
    public void clearNeedsRedraw(){
        this.chunk.setNeedsRedraw(false);
    }
    
}
