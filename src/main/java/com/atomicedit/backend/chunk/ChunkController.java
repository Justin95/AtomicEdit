
package com.atomicedit.backend.chunk;

import com.atomicedit.backend.BlockCoord;
import com.atomicedit.backend.BlockType;
import com.atomicedit.backend.entity.Entity;
import com.atomicedit.backend.BlockEntity.BlockEntity;
import com.atomicedit.backend.nbt.MalformedNbtTagException;
import com.atomicedit.backend.nbt.NbtTag;

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
        return this.chunk;
    }
    
    
    public abstract void setBiomeAt(BlockCoord coord, int biome) throws MalformedNbtTagException;
    
    public abstract void setBlockAt(BlockCoord coord, BlockType block) throws MalformedNbtTagException;
    
    public abstract void setBlocks(int subChunkIndex, short[] blocks) throws MalformedNbtTagException;
    
    public abstract void addEntity(Entity entity) throws MalformedNbtTagException;
    
    public abstract void removeEntity(Entity entity) throws MalformedNbtTagException;
    
    public abstract void addBlockEntity(BlockEntity tileEntity) throws MalformedNbtTagException;
    
    public abstract void removeBlockEntity(BlockEntity tileEntity) throws MalformedNbtTagException;;
    
    public abstract void setChunkNbtTag(NbtTag tag) throws MalformedNbtTagException; //call this after editing the NbtTag from getChunkAsNbtTag
    
    
}
