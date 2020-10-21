
package atomicedit.backend;

import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.backend.chunk.Chunk;
import atomicedit.backend.chunk.ChunkSection;
import java.util.Iterator;

/**
 *
 * @author Justin Bonner
 */
public class BlockCoord {
    
    public final int x;
    public final int y;
    public final int z;
    
    public BlockCoord(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public int getSubChunkIndex(){
        if (y < 0) {
            return -(-y / ChunkSection.SIDE_LENGTH);
        }
        return y / ChunkSection.SIDE_LENGTH;
    }
    
    public int getChunkLocalX(){
        return x & 15;
    }
    
    public int getSubChunkLocalY(){
        return y & 15;
    }
    
    public int getChunkLocalZ(){
        return z & 15;
    }
    
    /**
     * Gets the coordinates of the chunk this block coordinate is in.
     * @return 
     */
    public ChunkCoord getChunkCoord(){
        return ChunkCoord.getInstance(Math.floorDiv(x, Chunk.X_LENGTH), Math.floorDiv(z, Chunk.Z_LENGTH));
    }
    
    public ChunkSectionCoord getChunkSectionCoord(){
        return new ChunkSectionCoord(Math.floorDiv(x, ChunkSection.SIDE_LENGTH), Math.floorDiv(y, ChunkSection.SIDE_LENGTH), Math.floorDiv(z, ChunkSection.SIDE_LENGTH));
    }
    
    @Override
    public String toString() {
        return "(x:" + x + ", y:" + y + ", z:" + z + ")";
    }
    
    public Iterator<BlockCoord> getNeighbors(){
        return new Iterator(){
            private int index = 0;
            
            @Override
            public boolean hasNext(){
                return index < 6;
            }
            
            @Override
            public BlockCoord next(){ //TODO not give coords in void or above world
                switch(index++){ //increment index after switch statement
                    case 0:
                        return new BlockCoord(x - 1, y, z);
                    case 1:
                        return new BlockCoord(x, y, z - 1);
                    case 2:
                        return new BlockCoord(x, y - 1, z);
                    case 3:
                        return new BlockCoord(x + 1, y, z);
                    case 4:
                        return new BlockCoord(x, y, z + 1);
                    case 5:
                        return new BlockCoord(x, y + 1, z);
                }
                throw new NullPointerException("Iterator called too many times");
            }
        };
    }
    
    
}
