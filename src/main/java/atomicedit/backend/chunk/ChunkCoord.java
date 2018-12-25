
package atomicedit.backend.chunk;

import atomicedit.backend.BlockCoord;

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
    
    
    public static ChunkCoord getInstanceFromWorldPos(float x, float z){
        return new ChunkCoord((int)Math.floor(x / Chunk.X_LENGTH), (int)Math.floor(z / Chunk.Z_LENGTH));
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
    
    @Override
    public String toString(){
        return "{"+x+","+z+"}";
    }
    
    @Override
    public int hashCode(){
        return (x * 10000) + z;
    }
    
    @Override
    public boolean equals(Object other){
        if(!(other instanceof ChunkCoord)) return false;
        return ((ChunkCoord) other).x == this.x && ((ChunkCoord) other).z == this.z;
    }
    
}
