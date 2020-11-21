
package atomicedit.backend.entity;

import atomicedit.backend.BlockCoord;
import atomicedit.backend.chunk.ChunkSection;
import org.joml.Vector3f;

/**
 *
 * @author Justin Bonner
 */
public class EntityCoord {
    
    public final double x;
    public final double y;
    public final double z;
    
    public EntityCoord(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public BlockCoord getBlockCoord(){
        return new BlockCoord(roundDown(x), roundDown(y), roundDown(z));
    }
    
    private static int roundDown(double d){
        return (int)Math.floor(d);
    }
    
    public Vector3f getChunkRelativePosition() {
        final int len = ChunkSection.SIDE_LENGTH;
        return new Vector3f(
            (float) (((x % len) + len) % len),
            (float) y,
            (float) (((z % len) + len) % len)
        );
    }
    
}
