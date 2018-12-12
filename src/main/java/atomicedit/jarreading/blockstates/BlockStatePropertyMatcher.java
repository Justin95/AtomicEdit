
package atomicedit.jarreading.blockstates;

import atomicedit.backend.BlockStateProperty;
import java.util.List;

/**
 *
 * @author Justin Bonner
 */
public class BlockStatePropertyMatcher {
    
    List<BlockStateProperty> mustHaves;
    
    public BlockStatePropertyMatcher(List<BlockStateProperty> mustHaves){
        this.mustHaves = mustHaves;
    }
    
    public boolean matches(BlockStateProperty[] properties){
        if(properties == null){
            return mustHaves.isEmpty();
        }
        outer:
        for(BlockStateProperty mustHave : mustHaves){
            for(BlockStateProperty property : properties){
                if(equalProps(mustHave, property)){
                    continue outer;
                }
            }
            return false;
        }
        return true;
    }
    
    /**
     * Determine if two BlockStateProperty objects are equal. Must make sure that types do not have to match
     * for example a boolean property with a value of true should equal a property with a value of "true".
     * That is the unfortunate nature of the way the data is laid out.
     * @param a
     * @param b
     * @return 
     */
    private boolean equalProps(BlockStateProperty a, BlockStateProperty b){
        return a.NAME.equals(b.NAME) && a.VALUE.toString().equalsIgnoreCase(b.VALUE.toString());
    }
    
}
