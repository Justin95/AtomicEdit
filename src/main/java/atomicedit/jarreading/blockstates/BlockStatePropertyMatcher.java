
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
    
    /**
     * Determine if these block state properties match this matcher.
     * The more in common the bigger the number, if they do not match return -1.
     * @param properties
     * @return 
     */
    public int matchScore(BlockStateProperty[] properties){
        if(properties == null){
            return mustHaves.isEmpty() ? 0 : -1;
        }
        int commonCount = 0;
        outer:
        for(BlockStateProperty mustHave : mustHaves){
            for(BlockStateProperty property : properties){
                if(equalProps(mustHave, property)){
                    commonCount++;
                    continue outer;
                }
            }
            return -1;
        }
        return commonCount;
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
    
    @Override
    public String toString(){
        return "BlockStatePropertyMatcher{mustHaves:" + mustHaves.toString() + "}";
    }
    
}
