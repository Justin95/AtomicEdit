
package atomicedit.volumes;

import atomicedit.backend.BlockCoord;
import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.backend.utils.BitArray;
import java.util.Collection;
import org.joml.Vector3i;

/**
 * A volume associated with a specific point in the world.
 * @author Justin Bonner
 */
public class WorldVolume extends Volume {
    
    private final BlockCoord smallestPoint;
    
    public WorldVolume(Box enclosingBox, BitArray includedSet, BlockCoord smallestPoint){
        super(enclosingBox, includedSet);
        this.smallestPoint = smallestPoint;
    }
    
    public WorldVolume(Volume volume, BlockCoord smallestPoint){
        this(volume.getEnclosingBox(), volume.getIncludedSet(), smallestPoint);
    }
    
    public static WorldVolume getInstance(Vector3i pointA, Vector3i pointB) {
        Volume volume = Volume.getInstance(pointA, pointB);
        BlockCoord smallestCoord = new BlockCoord(Math.min(pointA.x, pointB.x), Math.min(pointA.y, pointB.y), Math.min(pointA.z, pointB.z));
        return new WorldVolume(volume, smallestCoord);
    }
    
    public static WorldVolume getInstance(Volume volume, BlockCoord smallestPoint) {
        return new WorldVolume(volume, smallestPoint);
    }
    
    public BlockCoord getSmallestPoint(){
        return this.smallestPoint;
    }
    
    public boolean containsCoord(BlockCoord coord){
        return containsCoord(coord.x, coord.y, coord.z);
    }
    
    /**
     * Checks if this WorldVolume contains the point in world coordinates.
     * @param x in world coordinates
     * @param y in world coordinates
     * @param z in world coordinates
     * @return 
     */
    public boolean containsCoord(int x, int y, int z){
        return this.containsXYZ(x - smallestPoint.x, y - smallestPoint.y, z - smallestPoint.z);
    }
    
    public Collection<ChunkCoord> getContainedChunkCoords(){
        return this.getContainedChunkCoords(smallestPoint);
    }
    
}
