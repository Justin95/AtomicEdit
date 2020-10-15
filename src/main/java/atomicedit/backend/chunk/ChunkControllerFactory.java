
package atomicedit.backend.chunk;

import atomicedit.backend.nbt.MalformedNbtTagException;

/**
 *
 * @author Justin Bonner
 */
public class ChunkControllerFactory {
    
    
    
    public static ChunkController getChunkController(Chunk chunk) throws MalformedNbtTagException{
        for(ChunkControllerType option : ChunkControllerType.values()){
            if(option.canWorkWith(chunk)){
                return option.getChunkController(chunk);
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
            chunk -> between(chunk.getChunkTag().getIntTag("DataVersion").getPayload(), 1519, 1952), //1519 is full release minecraft 1.13
            chunk -> new ChunkController1_13(chunk)
        ),
        VERSION_1_14(
            chunk -> between(chunk.getChunkTag().getIntTag("DataVersion").getPayload(), 1952, 2566), //1952 is full release minecraft 1.14
            chunk -> new ChunkController1_14(chunk)
        ),
        VERSION_1_16(
            chunk -> chunk.getChunkTag().getIntTag("DataVersion").getPayload() >= 2566, //2566 is full release minecraft 1.16
            chunk -> new ChunkController1_16(chunk)
        ),
        ;
        
        private final ChunkVersionChecker checker;
        private final ChunkControllerGetter getter;
        
        ChunkControllerType(ChunkVersionChecker checker, ChunkControllerGetter getter){
            this.checker = checker;
            this.getter = getter;
        }
        
        public boolean canWorkWith(Chunk chunk){
            try{
                return this.checker.isCorrectVersion(chunk);
            }catch(MalformedNbtTagException e){
                return false;
            }
        }
        
        public ChunkController getChunkController(Chunk chunk) throws MalformedNbtTagException{
            return this.getter.getChunkController(chunk);
        }
        
        private interface ChunkVersionChecker{
            public boolean isCorrectVersion(Chunk chunk) throws MalformedNbtTagException;
        }
        
        private interface ChunkControllerGetter{
            public ChunkController getChunkController(Chunk chunk) throws MalformedNbtTagException;
        }
    }
    
}
