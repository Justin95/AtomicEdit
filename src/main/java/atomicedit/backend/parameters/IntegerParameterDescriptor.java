
package atomicedit.backend.parameters;

/**
 *
 * @author Justin Bonner
 */
public class IntegerParameterDescriptor extends ParameterDescriptor<Integer> {
    
    public final int maxAllowed;
    public final int minAllowed;
    
    public IntegerParameterDescriptor(String name, Integer defaultValue, int min, int max) {
        super(name, ParameterType.INT, defaultValue);
        this.maxAllowed = max;
        this.minAllowed = min;
    }
    
}
