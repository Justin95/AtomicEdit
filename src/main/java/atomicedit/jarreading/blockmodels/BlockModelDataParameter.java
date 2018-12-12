
package atomicedit.jarreading.blockmodels;

/**
 *
 * @author Justin Bonner
 */
public class BlockModelDataParameter {
    
    public final String name;
    public final ParameterType type;
    public final Object value;
    
    
    public BlockModelDataParameter(String name, Integer value){
        this.name = name;
        this.type = ParameterType.INTEGER;
        this.value = value;
    }
    
    public BlockModelDataParameter(String name, Boolean value){
        this.name = name;
        this.type = ParameterType.BOOLEAN;
        this.value = value;
    }
    public BlockModelDataParameter(String name, String value){
        this.name = name;
        this.type = ParameterType.STRING;
        this.value = value;
    }
    
    @Override
    public String toString(){
        return "{" + name + ", " + value + "}";
    }
    
    public static enum ParameterType{
        INTEGER, BOOLEAN, STRING
    }
    
}
