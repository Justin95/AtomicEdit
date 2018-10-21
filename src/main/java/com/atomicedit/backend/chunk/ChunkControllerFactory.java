
package com.atomicedit.backend.chunk;

import com.atomicedit.backend.nbt.MalformedNbtTagException;

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
    
    
    
    private enum ChunkControllerType{
        VERSION_1_13(
            chunk -> chunk.getChunkTag().getIntTag("DataVersion").getPayload() >= 1519, //1519 is full release minecraft 1.13
            chunk -> new ChunkController1_13(chunk)
        )
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
