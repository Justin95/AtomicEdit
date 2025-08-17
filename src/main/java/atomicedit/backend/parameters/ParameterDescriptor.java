
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
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ParameterDescriptor)) {
            return false;
        }
        ParameterDescriptor desc = (ParameterDescriptor) other;
        return this.name.equals(desc.name) && this.parameterType == desc.parameterType;
    }
    
}
