
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
    
    
    
}
