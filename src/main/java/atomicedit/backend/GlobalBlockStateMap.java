
package atomicedit.backend;

import atomicedit.logging.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Justin Bonner
 */
public class GlobalBlockStateMap {
    
    private static final Map<BlockState, Short> blockToIdMap = new HashMap<>();
    private static final ArrayList<BlockState> idToBlockTypeMap = new ArrayList<>(100);
    private static short idCounter = 0;
    
    public static void addBlockType(BlockState blockType){
        if(idToBlockTypeMap.contains(blockType)){
            return;
        }
        if(idToBlockTypeMap.size() != idCounter){
            Logger.error("Block type map id desync");
        }
        synchronized(idToBlockTypeMap){
            idToBlockTypeMap.add(blockType);
        }
        blockToIdMap.put(blockType, idCounter);
        idCounter++;
    }
    
    public static boolean hasBlockType(BlockState blockType){
        return idToBlockTypeMap.contains(blockType);
    }
    
    public static short getBlockId(BlockState blockType){
        return blockToIdMap.get(blockType);
    }
    
    public static BlockState getBlockType(short id){
        if(id < 0 || id >= idCounter) throw new IllegalArgumentException("Bad id used in looking up mapping: " + id);
        return idToBlockTypeMap.get(id);
    }
    
    public static List<BlockState> getBlockTypes(){
        synchronized(idToBlockTypeMap){
            return new ArrayList(idToBlockTypeMap);
        }
    }
    
}
