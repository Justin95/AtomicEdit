
package atomicedit.utils;

import atomicedit.backend.utils.GeneralUtils;
import org.apache.commons.lang3.StringUtils;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Justin Bonner
 */
public class ReadPackedArrayTest {
    
    
    @Test
    public void test(){
        long[] source = new long[]{
            0b0000011011000000000000000000000000000000000000000000000000000001L,
            0b1010001010100000000000000000000000000000000000000000000000000000L
        };
        int groupSize = 5;
        int elementIndex = 12;
        int result = GeneralUtils.readIntFromPackedLongArray(groupSize, elementIndex, source);
        int expectedResult = 0b00011;
        if(result != expectedResult){
            System.out.println("Result: " + StringUtils.leftPad(Integer.toBinaryString(result), groupSize, "0") + " Expected result: " + StringUtils.leftPad(Integer.toBinaryString(expectedResult), groupSize, "0"));
        }
        assertTrue(result == expectedResult);
    }
    
    
}
