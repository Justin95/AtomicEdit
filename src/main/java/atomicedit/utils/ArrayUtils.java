
package atomicedit.utils;

import java.util.List;

/**
 * These should all be built in functions.
 * @author Justin Bonner
 */
public class ArrayUtils {
    
    /**
     * Determine if an array contains all given values.
     * @param <T> the array type
     * @param lookIn the array to look in
     * @param lookFor the list of things to look for
     * @return true if the array contains all the values
     */
    public static <T> boolean containsAll(T[] lookIn, List<T> lookFor) {
        Outer:
        for (int i = 0; i < lookFor.size(); i++) {
            T look = lookFor.get(i);
            for (int j = 0; j < lookIn.length; j++) {
                if ((lookIn[j] != null && lookIn[j].equals(look)) || (lookIn[j] == null && look == null)) {
                    continue Outer;
                }
            }
            return false;
        }
        return true;
    }
    
    public static <T> boolean contains(T[][] source, T target) {
        for (int i = 0; i < source.length; i++) {
            for (int j = 0; j < source[i].length; j++) {
                if ((source[i][j] != null && source[i][j].equals(target)) || (source[i][j] == null && target == null)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Check if two float arrays are equal. They are equal if the
     * arrays have the same length and the floats at each index equal
     * each other within a tolerance of the 3 least significant mantissa bits.
     * @param a one float array
     * @param b another float array
     * @return true if each float is equal
     */
    public static boolean equals(float[] a, float[] b) {
        final int TOLERANCE_BITS = 3; //max lower bits the mantissa's of the floats my differ by
        final int MAX_DIFF = ~(~0 << TOLERANCE_BITS); 
        if (a == null | b == null || a.length != b.length) {
            return false;
        }
        for (int i = 0 ; i < a.length; i++) {
            int aBits = Float.floatToIntBits(a[i]);
            int bBits = Float.floatToIntBits(b[i]);
            if ((aBits & ~bBits) < MAX_DIFF) {
                //not equal
                return false;
            }
        }
        return true;
    }
    
}
