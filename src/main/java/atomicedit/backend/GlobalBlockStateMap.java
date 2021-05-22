
package atomicedit.backend;

import atomicedit.logging.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Justin Bonner
 */
public class GlobalBlockStateMap {
    
    private static final Map<BlockState, Short> blockToIdMap = new HashMap<>();
    private static final ArrayList<BlockState> idToBlockTypeMap = new ArrayList<>();
    private static final Map<Short, Short[]> idToRotatedBlockIdMap = new HashMap<>();
    private static final Map<Short, Short> idToFlippedBlockIdMap = new HashMap<>();
    private static volatile short idCounter = 0;
    
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
        if (idCounter == Short.MAX_VALUE) {
            Logger.critical("Too many loaded block types: " + idCounter);
            throw new IllegalStateException("Too many loaded block types!");
        }
    }
    
    public static boolean hasBlockType(BlockState blockType){
        return idToBlockTypeMap.contains(blockType);
    }
    
    public static short getBlockId(BlockState blockType){
        return blockToIdMap.get(blockType);
    }
    
    public static BlockState getBlockType(short id){
        if (id < 0 || id >= idCounter) {
            throw new IllegalArgumentException("Bad id used in looking up mapping: " + id);
        }
        return idToBlockTypeMap.get(id);
    }
    
    public static List<BlockState> getBlockTypes(){
        synchronized(idToBlockTypeMap){
            return new ArrayList(idToBlockTypeMap);
        }
    }
    
    public static short getRotatedBlockId(short blockId, int rightRots) {
        rightRots %= 4;
        if (rightRots == 0) {
            return blockId;
        }
        Short[] rotatedIds = idToRotatedBlockIdMap.get(blockId);
        if (rotatedIds == null) {
            BlockState blockState = idToBlockTypeMap.get(blockId);
            rotatedIds = new Short[] {
                blockToIdMap.get(BlockStateRotationUtil.guessRotatedBlockState(blockState, 1)),
                blockToIdMap.get(BlockStateRotationUtil.guessRotatedBlockState(blockState, 2)),
                blockToIdMap.get(BlockStateRotationUtil.guessRotatedBlockState(blockState, 3))
            };
            idToRotatedBlockIdMap.put(
                blockId,
                rotatedIds
            );
        }
        return rotatedIds[rightRots - 1]; //one rotation at index 0, etc
    }
    
    /**
     * Get the flipped block type.
     * @param blockId
     * @return 
     */
    public static short getFlippedBlockId(short blockId) {
        Short flippedId = idToFlippedBlockIdMap.get(blockId);
        if (flippedId == null) {
            BlockState blockState = idToBlockTypeMap.get(blockId);
            BlockState flippedState = BlockStateRotationUtil.guessFlippedBlockState(blockState);
            flippedId = blockToIdMap.get(flippedState);
            idToFlippedBlockIdMap.put(blockId, flippedId);
        }
        return flippedId;
    }
    
    public static int getNumBlockStates() {
        return idCounter;
    }
    
}
