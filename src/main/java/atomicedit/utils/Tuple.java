
package atomicedit.utils;

/**
 * 
 * @author Justin Bonner
 * @param <T>
 * @param <U> 
 */
public class Tuple <T, U> {
    
    public final T left;
    public final U right;
    
    public Tuple(T left, U right) {
        this.left = left;
        this.right = right;
    }
    
    @Override
    public String toString() {
        return "{" + left.toString() + ", " + right.toString() + "}";
    }
    
}
