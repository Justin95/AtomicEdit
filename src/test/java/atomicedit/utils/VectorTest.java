
package atomicedit.utils;

import org.joml.Vector3f;
import org.junit.Test;

/**
 *
 * @author Justin Bonner
 */
public class VectorTest {
    
    @Test
    public void test(){
        Vector3f vec = new Vector3f(1,0,0);
        vec.rotateY(-(float)Math.toRadians(90));
        System.out.println("output: " + vec);
    }
    
}
