
package com.atomicedit.backend.chunk;

import com.atomicedit.backend.BlockCoord;

/**
 *
 * @author Justin Bonner
 */
public class ChunkCoord {
    
    public final int x;
    public final int z;
    
    public ChunkCoord(int x, int z){
        this.x = x;
        this.z = z;
    }
    
    /**
     * Get the block coordinate in this chunk that is closest to negative infinity on x, y, and z
     * @return 
     */
    public BlockCoord getMinBlockCoord(){
        return new BlockCoord(x * Chunk.X_LENGTH, 0, z * Chunk.Z_LENGTH);
    }
    
    public BlockCoord getMaxBlockCoord(){
        return new BlockCoord(x * Chunk.X_LENGTH + Chunk.X_LENGTH - 1,
                              Chunk.NUM_CHUNK_SECTIONS_IN_CHUNK * ChunkSection.SIDE_LENGTH,
                              z * Chunk.Z_LENGTH + Chunk.Z_LENGTH - 1
        );
    }
    
}
