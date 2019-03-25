
package atomicedit.utils;

import org.joml.Vector3f;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Justin Bonner
 */
public class RotationToDirectionTest {
    
    private static final float ALLOWED_DIFF = 0.0001f;
    
    @Test
    public void test1(){
        Vector3f rotation = new Vector3f(0,0,0); //z in rotation vector unused
        Vector3f expectedResult = new Vector3f(0,0,-1);
        Vector3f result = MathUtils.rotationVectorToDirectionVector(rotation);
        assertTrue(closeEnough(result, expectedResult));
    }
    
    @Test
    public void test2(){
        Vector3f rotation = new Vector3f(-90,0,0); //z in rotation vector unused
        Vector3f expectedResult = new Vector3f(0,1,0);
        Vector3f result = MathUtils.rotationVectorToDirectionVector(rotation);
        //System.out.println("Expected: " + expectedResult + " Result: " + result);
        assertTrue(closeEnough(result, expectedResult));
    }
    
    @Test
    public void test3(){
        Vector3f rotation = new Vector3f(0,90,0); //z in rotation vector unused
        Vector3f expectedResult = new Vector3f(1,0,0);
        Vector3f result = MathUtils.rotationVectorToDirectionVector(rotation);
        //System.out.println("Expected: " + expectedResult + " Result: " + result);
        assertTrue(closeEnough(result, expectedResult));
    }
    
    @Test
    public void test4(){
        Vector3f rotation = new Vector3f(0,180,0); //z in rotation vector unused
        Vector3f expectedResult = new Vector3f(0,0,1);
        Vector3f result = MathUtils.rotationVectorToDirectionVector(rotation);
        //System.out.println("Expected: " + expectedResult + " Result: " + result);
        assertTrue(closeEnough(result, expectedResult));
    }
    
    private static boolean closeEnough(Vector3f a, Vector3f b){
        if(Math.abs(a.x - b.x) > ALLOWED_DIFF){
            return false;
        }
        if(Math.abs(a.y - b.y) > ALLOWED_DIFF){
            return false;
        }
        if(Math.abs(a.z - b.z) > ALLOWED_DIFF){
            return false;
        }
        return true;
    }
    
}
