
package atomicedit.backend.chunk;

import atomicedit.backend.nbt.MalformedNbtTagException;
import atomicedit.backend.nbt.NbtCompoundTag;

/**
 *
 * @author Justin Bonner
 */
public class ChunkControllerFactory {
    
    
    
    static ChunkNbtInterpreter getChunkNbtInterpreter(NbtCompoundTag chunkNbtTag) throws MalformedNbtTagException{
        for(ChunkControllerType option : ChunkControllerType.values()){
            if(option.canWorkWith(chunkNbtTag)){
                return option.getChunkController(chunkNbtTag);
            }
        }
        throw new MalformedNbtTagException("Chunk is either corrupt or of unsupported format");
    }
    
    /**
     * Check if an int, check, is between min (inclusive) and max (exclusive).
     * @param check
     * @param min
     * @param max
     * @return 
     */
    private static boolean between(int check, int min, int max) {
        return check >= min && check < max;
    }
    
    private enum ChunkControllerType{
        VERSION_1_13(
            chunkNbt -> between(chunkNbt.getIntTag("DataVersion").getPayload(), 1519, 1952), //1519 is full release minecraft 1.13
            chunkNbt -> new ChunkController1_13()
        ),
        VERSION_1_14(
            chunkNbt -> between(chunkNbt.getIntTag("DataVersion").getPayload(), 1952, 2566), //1952 is full release minecraft 1.14
            chunkNbt -> new ChunkController1_14()
        ),
        VERSION_1_16(
            chunkNbt -> chunkNbt.getIntTag("DataVersion").getPayload() >= 2566, //2566 is full release minecraft 1.16
            chunkNbt -> new ChunkController1_16()
        ),
        ;
        
        private final ChunkVersionChecker checker;
        private final ChunkControllerGetter getter;
        
        ChunkControllerType(ChunkVersionChecker checker, ChunkControllerGetter getter){
            this.checker = checker;
            this.getter = getter;
        }
        
        public boolean canWorkWith(NbtCompoundTag chunkTag){
            try{
                return this.checker.isCorrectVersion(chunkTag);
            }catch(MalformedNbtTagException e){
                return false;
            }
        }
        
        public ChunkNbtInterpreter getChunkController(NbtCompoundTag chunkNbt) throws MalformedNbtTagException{
            return this.getter.getChunkController(chunkNbt);
        }
        
        private interface ChunkVersionChecker{
            public boolean isCorrectVersion(NbtCompoundTag chunkTag) throws MalformedNbtTagException;
        }
        
        private interface ChunkControllerGetter{
            public ChunkNbtInterpreter getChunkController(NbtCompoundTag chunkNbt) throws MalformedNbtTagException;
        }
    }
    
}
