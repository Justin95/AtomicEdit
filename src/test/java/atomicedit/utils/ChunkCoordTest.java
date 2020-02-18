
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
        ChunkCoord a = ChunkCoord.getInstance(10,10);
        ChunkCoord b = ChunkCoord.getInstance(10,10);
        ChunkCoord c = ChunkCoord.getInstance(1,3);
        assertTrue(a.equals(b));
        assertTrue(!a.equals(c));
    }
    
    
    
}
