
package atomicedit.operations.utils;

/**
 *
 * @author Justin Bonner
 */
public class OperationParameterDescriptor {
    
    public final String name;
    public final OperationParameterType parameterType;
    
    public OperationParameterDescriptor(String name, OperationParameterType paramType){
        this.name = name;
        this.parameterType = paramType;
    }
    
}
