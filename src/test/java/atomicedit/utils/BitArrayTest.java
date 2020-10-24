
package atomicedit.utils;

import atomicedit.backend.utils.BitArray;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Justin Bonner
 */
public class BitArrayTest {
    
    @Test
    public void testBitArray(){
        BitArray testArray = new BitArray(10, false);
        assertFalse(testArray.get(0));
        assertFalse(testArray.get(9));
        
        BitArray testArray2 = new BitArray(10000, true);
        assertTrue(testArray2.get(0));
        assertTrue(testArray2.get(9999));
        
        try{
            testArray2.get(-1);
            fail();
        }catch(ArrayIndexOutOfBoundsException e){
            //pass
        }
        
        try{
            testArray2.get(10000);
            fail();
        }catch(ArrayIndexOutOfBoundsException e){
            //pass
        }
        
        BitArray testArray3 = new BitArray(1000, false);
        testArray3.set(51, true);
        assertTrue(testArray3.get(51));
    }
    
}
