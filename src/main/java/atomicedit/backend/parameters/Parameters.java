
package atomicedit.backend.parameters;

import atomicedit.backend.BlockState;
import atomicedit.logging.Logger;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Justin Bonner
 */
public class Parameters {
    
    private final Map<ParameterDescriptor, Object> paramToValue;
    
    public Parameters(){
        paramToValue = new HashMap<>();
    }
    
    public void setParam(ParameterDescriptor descriptor, Object value){
        paramToValue.put(descriptor, value);
    }
    
    public String getParamAsString(ParameterDescriptor key){
        if(key.parameterType != ParameterType.STRING){
            Logger.error("Tried to get operation parameter '" + key.name + "' as a String but it isn't a String");
            throw new IllegalArgumentException("Tried to get operation parameter as wrong type");
        }
        if(!paramToValue.containsKey(key)){
            Logger.error("Parameters did not contain key: " + key.name);
            return null;
        }
        Object value = paramToValue.get(key);
        if(value == null){
            return null;
        }
        if(!(value instanceof String)){
            Logger.error("Parameter was not a String, it was " + value.getClass());
            throw new RuntimeException("Parameter was not a String");
        }
        return (String)value;
    }
    
    public Integer getParamAsInteger(ParameterDescriptor key){
        if(key.parameterType != ParameterType.INT){
            Logger.error("Tried to get operation parameter '" + key.name + "' as a Integer but it isn't an Integer");
            throw new IllegalArgumentException("Tried to get operation parameter as wrong type");
        }
        if(!paramToValue.containsKey(key)){
            Logger.error("Parameters did not contain key: " + key.name);
            return null;
        }
        Object value = paramToValue.get(key);
        if(value == null){
            return null;
        }
        if(!(value instanceof Integer)){
            Logger.error("Parameter was not an Integer, it was " + value.getClass());
            throw new RuntimeException("Parameter was not an Integer");
        }
        return (Integer)value;
    }
    
    public Float getParamAsFloat(ParameterDescriptor key){
        if(key.parameterType != ParameterType.FLOAT){
            Logger.error("Tried to get operation parameter '" + key.name + "' as a Float but it isn't a Float");
            throw new IllegalArgumentException("Tried to get operation parameter as wrong type");
        }
        if(!paramToValue.containsKey(key)){
            Logger.error("Parameters did not contain key: " + key.name);
            return null;
        }
        Object value = paramToValue.get(key);
        if(value == null){
            return null;
        }
        if(!(value instanceof Float)){
            Logger.error("Parameter was not a Float, it was " + value.getClass());
            throw new RuntimeException("Parameter was not a Float");
        }
        return (Float)value;
    }
    
    public BlockState getParamAsBlockState(ParameterDescriptor key){
        if(key.parameterType != ParameterType.BLOCK_SELECTOR){
            Logger.error("Tried to get operation parameter '" + key.name + "' as a Block State but it isn't a Block State");
            throw new IllegalArgumentException("Tried to get operation parameter as wrong type");
        }
        if(!paramToValue.containsKey(key)){
            Logger.error("Parameters did not contain key: " + key.name);
            return null;
        }
        Object value = paramToValue.get(key);
        if(value == null){
            return null;
        }
        if(!(value instanceof BlockState)){
            Logger.error("Parameter was not a Block State, it was " + value.getClass());
            throw new RuntimeException("Parameter was not a Block State");
        }
        return (BlockState)value;
    }
    
}
