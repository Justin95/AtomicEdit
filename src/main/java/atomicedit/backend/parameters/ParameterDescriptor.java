
package atomicedit.backend.parameters;

/**
 *
 * @author Justin Bonner
 */
public abstract class ParameterDescriptor<T> {
    
    public final String name;
    public final ParameterType parameterType;
    public final T defaultValue;
    
    public ParameterDescriptor(String name, ParameterType paramType, T defaultValue){
        this.name = name;
        this.parameterType = paramType;
        this.defaultValue = defaultValue;
    }
    
}
