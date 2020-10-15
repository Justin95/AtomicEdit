
package atomicedit.utils;

import atomicedit.backend.utils.GeneralUtils;
import org.apache.commons.lang3.StringUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Justin Bonner
 */
public class ReadPackedArrayTest {
    
    @Test
    public void testWriteArray(){
        int[] source = new int[]{1, 5, 1, 63, 44, 23};
        int numBitsEach = 21;
        long[] result = GeneralUtils.writeIntArrayToPackedLongArray(source, numBitsEach);
        int testIndex = 4;
        int intResult = GeneralUtils.readIntFromPackedLongArray(numBitsEach, testIndex, result);
        if(intResult != source[testIndex]){
            System.out.println("Result: " + longArrayToString(result));
        }
        assertEquals(source[testIndex], intResult);
    }
    
    @Test
    public void test(){
        long[] source = new long[]{
            0b0010011011000000000000000000000000000000000000000000000000000001L,
            0b1010001010100000000000000000000000000000000000000000000000000001L
        };
        int groupSize = 5;
        int elementIndex = 12;
        int result = GeneralUtils.readIntFromPackedLongArray(groupSize, elementIndex, source);
        int expectedResult = 0b10010;
        if(result != expectedResult){
            System.out.println("Result: " + StringUtils.leftPad(Integer.toBinaryString(result), groupSize, "0") + " Expected result: " + StringUtils.leftPad(Integer.toBinaryString(expectedResult), groupSize, "0"));
        }
        assertTrue(result == expectedResult);
    }
    
    private String longArrayToString(long[] longs){
        String result = "";
        for(long element : longs){
            result += StringUtils.leftPad(Long.toBinaryString(element), 64, "0") + " ";
        }
        return result.trim();
    }
    
    @Test
    public void testNonPackedArray() {
        long[] source = new long[]{
            0b0010011011000000000000000000000000000000000000000000000110100001L,
            0b1010001010100000000000000000000000000000000000000000000001101001L
        };
        int groupSize = 5;
        int elementIndex = 12;
        int result = GeneralUtils.readIntFromLongArray(groupSize, elementIndex, source);
        int expectedResult = 0b01001;
        if(result != expectedResult){
            System.out.println("Result: " + StringUtils.leftPad(Integer.toBinaryString(result), groupSize, "0") + " Expected result: " + StringUtils.leftPad(Integer.toBinaryString(expectedResult), groupSize, "0"));
        }
        assertTrue(result == expectedResult);
    }
    
}
