
package atomicedit.backend.parameters;

/**
 *
 * @author Justin Bonner
 */
public class FloatParameterDescriptor extends ParameterDescriptor<Float> {
    
    public final float minAllowed;
    public final float maxAllowed;
    
    public FloatParameterDescriptor(String name, Float defaultValue, float min, float max) {
        super(name, ParameterType.FLOAT, defaultValue);
        this.minAllowed = min;
        this.maxAllowed = max;
    }
    
}
