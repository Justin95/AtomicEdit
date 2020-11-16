
package atomicedit.backend.chunk;

import atomicedit.backend.BlockCoord;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Justin Bonner
 */
public class ChunkCoord {
    
    private static final Map<ChunkCoord, ChunkCoord> instanceCache = new HashMap<>();
    
    public final int x;
    public final int z;
    
    private ChunkCoord(int x, int z){
        this.x = x;
        this.z = z;
    }
    
    public static ChunkCoord getInstance(int x, int z) {
        ChunkCoord coord = new ChunkCoord(x, z);
        if (instanceCache.containsKey(coord)) {
            return instanceCache.get(coord);
        }
        instanceCache.put(coord, coord); //strange but can't get from a set
        return coord;
    }
    
    public static ChunkCoord getInstanceFromWorldPos(float x, float z){
        return ChunkCoord.getInstance((int)Math.floor(x / Chunk.X_LENGTH), (int)Math.floor(z / Chunk.Z_LENGTH));
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
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + this.x;
        hash = 19 * hash + this.z;
        return hash;
    }
    
    
    @Override
    public boolean equals(Object obj){
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ChunkCoord)) {
            return false;
        }
        ChunkCoord other = (ChunkCoord) obj;
        return other.x == this.x && other.z == this.z;
    }
    
    /**
     * Create a list of chunk coords containing every given chunk coord and
     * their adjacent chunk coordinates.
     * @param baseCoords the starting chunk coords
     * @return those chunk coords and the ones adjacent
     */
    public static List<ChunkCoord> expandToAdjacentCoords(List<ChunkCoord> baseCoords) {
        List<ChunkCoord> baseAndAdjCoords = new ArrayList<>(baseCoords);
        for(ChunkCoord baseCoord : baseCoords) {
            ChunkCoord[] adjcents = new ChunkCoord[] {
                ChunkCoord.getInstance(baseCoord.x + 1, baseCoord.z),
                ChunkCoord.getInstance(baseCoord.x - 1, baseCoord.z),
                ChunkCoord.getInstance(baseCoord.x, baseCoord.z + 1),
                ChunkCoord.getInstance(baseCoord.x, baseCoord.z - 1)
            };
            for(ChunkCoord adjcent : adjcents) {
                if(!baseAndAdjCoords.contains(adjcent)) {
                    baseAndAdjCoords.add(adjcent);
                }
            }
        }
        return baseAndAdjCoords;
    }
    
    public int distanceSquaredFrom(int xPos, int zPos) {
        return (xPos - x) * (xPos - x) + (zPos - z) * (zPos - z);
    }
    
}
