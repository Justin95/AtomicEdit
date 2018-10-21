
package com.atomicedit.backend;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Justin Bonner
 */
public class BlockType {
    
    public final String name;
    public final BlockStateProperty[] blockStateProperties;
    
    /**
     * Only have one immutable BlockType object per block state
     */
    private static HashMap<String, ArrayList<BlockType>> blockLibrary = new HashMap<>();
    public static final BlockType AIR; //need a block to fill empty chunk sections with
    static{
        AIR = getBlockType("minecraft:air", null);
    }
    private BlockType(String name, BlockStateProperty[] blockStateProperties){
        if(name == null) throw new IllegalArgumentException("Block name cannot be null");
        if(blockStateProperties != null && blockStateProperties.length == 0) blockStateProperties = null;
        this.name = name;
        this.blockStateProperties = blockStateProperties;
    }
    
    public static BlockType getBlockType(String name, BlockStateProperty[] blockStateProperties){
        if(blockStateProperties.length == 0){
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
            return newType;
        }
        ArrayList<BlockType> newList = new ArrayList<>();
        newList.add(newType);
        blockLibrary.put(name, newList);
        return newType;
    }
    
    
    public static String getLoadedBlockTypesDebugString(){
        StringBuilder strBuilder = new StringBuilder();
        for(String str : blockLibrary.keySet()){
            strBuilder.append(str);
            strBuilder.append("\n");
        }
        return strBuilder.toString();
    }
    
}
