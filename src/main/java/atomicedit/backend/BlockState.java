
package atomicedit.backend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author Justin Bonner
 */
public class BlockState {
    
    public final String name;
    public final BlockStateProperty[] blockStateProperties;
    private final String stringDesc;
    /**
     * Only have one immutable BlockState object per block state
     */
    private static HashMap<String, ArrayList<BlockState>> blockLibrary = new HashMap<>();
    public static final BlockState AIR = getBlockState("minecraft:air", null); //need a block to fill empty chunk sections with
    
    private BlockState(String name, BlockStateProperty[] blockStateProperties){
        if(name == null) throw new IllegalArgumentException("Block name cannot be null");
        if(blockStateProperties != null && blockStateProperties.length == 0){
            blockStateProperties = null;
        }
        this.name = name;
        this.blockStateProperties = blockStateProperties;
        this.stringDesc = "{" + name + ":" + (blockStateProperties != null ? Arrays.toString(blockStateProperties) : "[]") + "}";
    }
    
    public static BlockState getBlockState(String name, BlockStateProperty[] blockStateProperties){
        if(blockStateProperties != null && blockStateProperties.length == 0){
            blockStateProperties = null;
        }
        BlockState newType = new BlockState(name, blockStateProperties);
        if(blockLibrary.containsKey(name)){
            ArrayList<BlockState> potentialTypes = blockLibrary.get(name);
            for(BlockState type : potentialTypes){
                if(newType.equals(type)){
                    return type; //allow newType to be garbage collected
                }
            }
            potentialTypes.add(newType);
            GlobalBlockStateMap.addBlockType(newType);
            return newType;
        }
        ArrayList<BlockState> newList = new ArrayList<>();
        newList.add(newType);
        blockLibrary.put(name, newList);
        GlobalBlockStateMap.addBlockType(newType);
        return newType;
    }
    
    public static String getLoadedBlockTypesDebugString(){
        StringBuilder strBuilder = new StringBuilder();
        for(String str : blockLibrary.keySet()){
            strBuilder.append(blockLibrary.get(str));
            strBuilder.append("\n");
        }
        return strBuilder.toString();
    }
    
    @Override
    public String toString(){
        return stringDesc;
    }
    
    private boolean equals(BlockState other){
        return other.name.equals(this.name) && (Arrays.deepEquals(this.blockStateProperties, other.blockStateProperties));
    }
    
}
