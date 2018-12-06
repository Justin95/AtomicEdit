
package atomicedit.backend;

import atomicedit.logging.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Justin Bonner
 */
public class GlobalBlockTypeMap {
    
    private static Map<BlockType, Short> blockToIdMap = new HashMap<>();
    private static ArrayList<BlockType> idToBlockTypeMap = new ArrayList<>(100);
    private static short idCounter = 0;
    
    public static void addBlockType(BlockType blockType){
        if(idToBlockTypeMap.contains(blockType)){
            return;
        }
        if(idToBlockTypeMap.size() != idCounter){
            Logger.error("Block type map id desync");
        }
        idToBlockTypeMap.add(blockType);
        blockToIdMap.put(blockType, idCounter);
        idCounter++;
    }
    
    public static boolean hasBlockType(BlockType blockType){
        return idToBlockTypeMap.contains(blockType);
    }
    
    public static short getBlockId(BlockType blockType){
        return blockToIdMap.get(blockType);
    }
    
    public static BlockType getBlockType(short id){
        if(id < 0 || id >= idCounter) throw new IllegalArgumentException("Bad id used in looking up mapping: " + id);
        return idToBlockTypeMap.get(id);
    }
    
    
}
