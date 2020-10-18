
package atomicedit.backend.chunk;

import atomicedit.backend.nbt.NbtCompoundTag;
import atomicedit.backend.nbt.NbtTag;
import atomicedit.backend.nbt.MalformedNbtTagException;

/**
 *
 * @author Justin Bonner
 */
public interface ChunkParser {
    
    Chunk parseChunk(NbtCompoundTag chunkTag) throws MalformedNbtTagException;
    
    NbtTag writeToNbt(Chunk chunk) throws MalformedNbtTagException;
    
}
