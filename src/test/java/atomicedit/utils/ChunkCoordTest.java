
package atomicedit.utils;

import atomicedit.backend.chunk.ChunkCoord;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Justin Bonner
 */
public class ChunkCoordTest {
    
    
    @Test
    public void test(){
        ChunkCoord a = new ChunkCoord(10,10);
        ChunkCoord b = new ChunkCoord(10,10);
        ChunkCoord c = new ChunkCoord(1,3);
        assertTrue(a.equals(b));
        assertTrue(!a.equals(c));
    }
    
    
    
}
