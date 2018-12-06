
package atomicedit.backend.chunk;

import atomicedit.backend.nbt.MalformedNbtTagException;

/**
 *
 * @author Justin Bonner
 */
class ChunkUtils {
    
    
    /**
     * Reads the chunk version from a chunk in any format since Anvil.
     * @param chunk
     * @return
     * @throws MalformedNbtTagException 
     */
    public int readAnvilChunkVersion(Chunk chunk) throws MalformedNbtTagException{
        return chunk.getChunkTag().getIntTag("DataVersion").getPayload();
    }
    
    
    
    
    
}
