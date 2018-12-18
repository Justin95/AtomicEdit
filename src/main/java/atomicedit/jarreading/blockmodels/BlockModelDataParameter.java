
package atomicedit.jarreading.blockmodels;

/**
 *
 * @author Justin Bonner
 */
public class BlockModelDataParameter {
    
    public final ParameterType type;
    public final Object value;
    
    
    public BlockModelDataParameter(Integer value){
        this.type = ParameterType.INTEGER;
        this.value = value;
    }
    
    public BlockModelDataParameter(Boolean value){
        this.type = ParameterType.BOOLEAN;
        this.value = value;
    }
    public BlockModelDataParameter(String value){
        this.type = ParameterType.STRING;
        this.value = value;
    }
    
    @Override
    public String toString(){
        return "{type: " + type.name() + ", value: " + value + "}";
    }
    
    public static enum ParameterType{
        INTEGER, BOOLEAN, STRING
    }
    
}
