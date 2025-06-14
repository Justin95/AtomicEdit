
package atomicedit.utils;

import atomicedit.backend.ChunkSectionCoord;
import atomicedit.backend.chunk.ChunkCoord;
import org.junit.Assert;
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
        Assert.assertTrue(a.equals(b));
        Assert.assertTrue(!a.equals(c));
    }
    
    @Test
    public void testSectionY() {
        int innerSectionY = ChunkSectionCoord.getRelativeChunkSectionYFromWorldY(-2);
        Assert.assertTrue(innerSectionY == 14);
    }
    
    @Test
    public void test2() {
        int secY = ChunkSectionCoord.getChunkSectionYFromWorldY(-1);
        Assert.assertEquals(-1, secY);
    }
    
}
