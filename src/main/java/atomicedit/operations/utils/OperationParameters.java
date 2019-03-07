
package atomicedit.operations.utils;

import atomicedit.backend.BlockState;
import atomicedit.logging.Logger;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Justin Bonner
 */
public class OperationParameters {
    
    private final Map<OperationParameterDescriptor, Object> paramToValue;
    
    public OperationParameters(){
        paramToValue = new HashMap<>();
    }
    
    public void setParam(OperationParameterDescriptor descriptor, Object value){
        paramToValue.put(descriptor, value);
    }
    
    public String getParamAsString(OperationParameterDescriptor key){
        if(key.parameterType != OperationParameterType.STRING){
            Logger.error("Tried to get operation parameter '" + key.name + "' as a String but it isn't a String");
            throw new IllegalArgumentException("Tried to get operation parameter as wrong type");
        }
        if(!paramToValue.containsKey(key)){
            Logger.error("Operation parameters did not contain key: " + key.name);
            return null;
        }
        Object value = paramToValue.get(key);
        if(value == null){
            return null;
        }
        if(!(value instanceof String)){
            Logger.error("Operation parameter was not a String, it was " + value.getClass());
            throw new RuntimeException("Operation parameter was not a String");
        }
        return (String)value;
    }
    
    public Integer getParamAsInteger(OperationParameterDescriptor key){
        if(key.parameterType != OperationParameterType.INT){
            Logger.error("Tried to get operation parameter '" + key.name + "' as a Integer but it isn't an Integer");
            throw new IllegalArgumentException("Tried to get operation parameter as wrong type");
        }
        if(!paramToValue.containsKey(key)){
            Logger.error("Operation parameters did not contain key: " + key.name);
            return null;
        }
        Object value = paramToValue.get(key);
        if(value == null){
            return null;
        }
        if(!(value instanceof Integer)){
            Logger.error("Operation parameter was not an Integer, it was " + value.getClass());
            throw new RuntimeException("Operation parameter was not an Integer");
        }
        return (Integer)value;
    }
    
    public Float getParamAsFloat(OperationParameterDescriptor key){
        if(key.parameterType != OperationParameterType.FLOAT){
            Logger.error("Tried to get operation parameter '" + key.name + "' as a Float but it isn't a Float");
            throw new IllegalArgumentException("Tried to get operation parameter as wrong type");
        }
        if(!paramToValue.containsKey(key)){
            Logger.error("Operation parameters did not contain key: " + key.name);
            return null;
        }
        Object value = paramToValue.get(key);
        if(value == null){
            return null;
        }
        if(!(value instanceof Float)){
            Logger.error("Operation parameter was not a Float, it was " + value.getClass());
            throw new RuntimeException("Operation parameter was not a Float");
        }
        return (Float)value;
    }
    
    public BlockState getParamAsBlockState(OperationParameterDescriptor key){
        if(key.parameterType != OperationParameterType.BLOCK_SELECTOR){
            Logger.error("Tried to get operation parameter '" + key.name + "' as a Block State but it isn't a Block State");
            throw new IllegalArgumentException("Tried to get operation parameter as wrong type");
        }
        if(!paramToValue.containsKey(key)){
            Logger.error("Operation parameters did not contain key: " + key.name);
            return null;
        }
        Object value = paramToValue.get(key);
        if(value == null){
            return null;
        }
        if(!(value instanceof BlockState)){
            Logger.error("Operation parameter was not a Block State, it was " + value.getClass());
            throw new RuntimeException("Operation parameter was not a Block State");
        }
        return (BlockState)value;
    }
    
}
