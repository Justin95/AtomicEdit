
package atomicedit.backend.parameters;

/**
 *
 * @author Justin Bonner
 */
public class BooleanParameterDescriptor extends ParameterDescriptor<Boolean> {
    
    public BooleanParameterDescriptor(String name, Boolean defaultValue) {
        super(name, ParameterType.BOOLEAN, defaultValue);
    }
    
}
