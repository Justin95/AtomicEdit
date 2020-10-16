
package atomicedit.backend;

import atomicedit.logging.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Justin Bonner
 */
public class BlockStateProperty {
    
    private static final Map<String, List<BlockStateProperty>> propertyLibrary = new HashMap<>();
    
    public final String NAME;
    public final Object VALUE;
    public final BlockStateDataType valueType;
    
    
    public static BlockStateProperty getInstance(String name, String value){
        return getInstance(name, BlockStateDataType.STRING, value);
    }
    
    public static BlockStateProperty getInstance(String name, Integer value){
        return getInstance(name, BlockStateDataType.INTEGER, value);
    }
    
    public static BlockStateProperty getInstance(String name, Boolean value){
        return getInstance(name, BlockStateDataType.BOOLEAN, value);
    }
    
    private static BlockStateProperty getInstance(String name, BlockStateDataType type, Object value) {
        if (!propertyLibrary.containsKey(name)) {
            propertyLibrary.put(name, new ArrayList<>());
        }
        List<BlockStateProperty> possibilities = propertyLibrary.get(name);
        for (BlockStateProperty possibility : possibilities) {
            if (possibility.NAME.equals(name) && possibility.VALUE.equals(value) && possibility.valueType == type) {
                return possibility; //found
            }
        }
        //was not found
        BlockStateProperty newProp = new BlockStateProperty(name, type, value);
        possibilities.add(newProp);
        Logger.debug("Created new Block State Property: " + newProp);
        return newProp;
    }
    
    private BlockStateProperty(String name, BlockStateDataType type, Object value) {
        if (name == null) {
            throw new IllegalArgumentException("Block State Property name cannot be null.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Block State Property '" + name + "' type cannot be null.");
        }
        if (value == null) {
            throw new IllegalArgumentException("Block State Property '" + name + "' value cannot be null.");
        }
        this.NAME = name;
        this.VALUE = value;
        this.valueType = type;
    }
    
    public enum BlockStateDataType {
        BOOLEAN,
        INTEGER,
        STRING
    }
    
    @Override
    public String toString(){
        if (this.valueType == BlockStateDataType.STRING) {
            return NAME + ":\"" + VALUE + "\"";
        }
        return NAME + ":" + VALUE;
    }
    
    @Override
    public boolean equals(Object obj){
        if(!(obj instanceof BlockStateProperty)){
            return false;
        }
        BlockStateProperty other = (BlockStateProperty)obj;
        return this.NAME.equals(other.NAME)
            && this.VALUE.equals(other.VALUE)
            && this.valueType == other.valueType;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.NAME);
        hash = 53 * hash + Objects.hashCode(this.VALUE);
        return hash;
    }
    
    /**
     * Get a list of all loaded block state properties.
     * @return 
     */
    public static List<BlockStateProperty> getAllLoadedProperties() {
        List<BlockStateProperty> properties = new ArrayList<>();
        for (List<BlockStateProperty> props : propertyLibrary.values()) {
            properties.addAll(props);
        }
        return properties;
    }
    
}
