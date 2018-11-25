
package atomicedit.backend.entity;

import atomicedit.backend.BlockCoord;

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
    
}
