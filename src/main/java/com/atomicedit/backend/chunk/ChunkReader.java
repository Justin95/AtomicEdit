
package com.atomicedit.backend.chunk;

import com.atomicedit.backend.BlockCoord;
import com.atomicedit.backend.blockentity.BlockEntity;
import com.atomicedit.backend.BlockType;
import com.atomicedit.backend.entity.Entity;
import com.atomicedit.backend.nbt.MalformedNbtTagException;
import com.atomicedit.backend.nbt.NbtTag;
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
