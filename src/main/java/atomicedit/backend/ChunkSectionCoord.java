
package atomicedit.backend;

import atomicedit.backend.chunk.Chunk;
import atomicedit.backend.chunk.ChunkSection;

/**
 *
 * @author Justin Bonner
 */
public class ChunkSectionCoord {
    
    public final int x;
    public final int y;
    public final int z;
    
    public ChunkSectionCoord(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public static ChunkSectionCoord getInstanceFromWorldPos(float x, float y, float z){
        return new ChunkSectionCoord((int)Math.floor(x / ChunkSection.SIDE_LENGTH), (int)Math.floor(y / ChunkSection.SIDE_LENGTH), (int)Math.floor(z / ChunkSection.SIDE_LENGTH));
    }
    
    /**
     * Get the block coordinate in this chunk that is closest to negative infinity on x, y, and z
     * @return 
     */
    public BlockCoord getMinBlockCoord(){
        return new BlockCoord(x * Chunk.X_LENGTH, y * ChunkSection.SIDE_LENGTH, z * Chunk.Z_LENGTH);
    }
    
    public BlockCoord getMaxBlockCoord(){
        return new BlockCoord(x * Chunk.X_LENGTH + Chunk.X_LENGTH - 1,
                              y * ChunkSection.SIDE_LENGTH + ChunkSection.SIDE_LENGTH - 1,
                              z * Chunk.Z_LENGTH + Chunk.Z_LENGTH - 1
        );
    }
    
    @Override
    public String toString(){
        return "(x:" + x + ", y:" + y + ", z:" + z + ")";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + this.x;
        hash = 23 * hash + this.y;
        hash = 23 * hash + this.z;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ChunkSectionCoord other = (ChunkSectionCoord) obj;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        if (this.z != other.z) {
            return false;
        }
        return true;
    }
    
    
}
