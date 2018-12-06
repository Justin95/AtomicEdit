
package atomicedit.backend;

import atomicedit.logging.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author Justin Bonner
 */
public class BlockType {
    
    public final String name;
    public final BlockStateProperty[] blockStateProperties;
    private final String stringDesc;
    /**
     * Only have one immutable BlockType object per block state
     */
    private static HashMap<String, ArrayList<BlockType>> blockLibrary = new HashMap<>();
    public static final BlockType AIR = getBlockType("minecraft:air", null); //need a block to fill empty chunk sections with
    
    private BlockType(String name, BlockStateProperty[] blockStateProperties){
        if(name == null) throw new IllegalArgumentException("Block name cannot be null");
        if(blockStateProperties != null && blockStateProperties.length == 0) blockStateProperties = null;
        this.name = name;
        this.blockStateProperties = blockStateProperties;
        this.stringDesc = "{" + name + ":" + (blockStateProperties != null ? Arrays.toString(blockStateProperties) : "[]") + "}";
    }
    
    public static BlockType getBlockType(String name, BlockStateProperty[] blockStateProperties){
        if(blockStateProperties != null && blockStateProperties.length == 0){
            blockStateProperties = null;
        }
        BlockType newType = new BlockType(name, blockStateProperties);
        if(blockLibrary.containsKey(name)){
            ArrayList<BlockType> potentialTypes = blockLibrary.get(name);
            for(BlockType type : potentialTypes){
                if(newType.equals(type)){
                    return type; //allow newType to be garbage collected
                }
            }
            potentialTypes.add(newType);
            GlobalBlockTypeMap.addBlockType(newType);
            return newType;
        }
        ArrayList<BlockType> newList = new ArrayList<>();
        newList.add(newType);
        blockLibrary.put(name, newList);
        GlobalBlockTypeMap.addBlockType(newType);
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
    
    private boolean equals(BlockType other){
        return other.name.equals(this.name) && (Arrays.deepEquals(this.blockStateProperties, other.blockStateProperties));
    }
    
}
