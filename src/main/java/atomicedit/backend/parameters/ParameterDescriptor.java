
package atomicedit.backend.parameters;

/**
 *
 * @author Justin Bonner
 */
public class ParameterDescriptor {
    
    public final String name;
    public final ParameterType parameterType;
    public final Object defaultValue;
    
    public ParameterDescriptor(String name, ParameterType paramType, Object defaultValue){
        this.name = name;
        this.parameterType = paramType;
        this.defaultValue = defaultValue;
    }
    
}
