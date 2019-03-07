
package atomicedit.backend.worldformats;

import atomicedit.backend.chunk.Chunk;
import atomicedit.backend.chunk.ChunkController;
import atomicedit.backend.chunk.ChunkCoord;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Classes implementing this interface are used to read and write Minecraft world file formats, Anvil, MCregion etc.
 * If new features can't be written to an old format the features should be removed.
 * @author Justin Bonner
 */
public interface WorldFormat {
    
    /**
     * Set the world to be worked with. The filepath to the top directory of a world.
     * @param filepath 
     */
    public void setWorld(String filepath);
    
    public void writeChunks(Map<ChunkCoord, ChunkController> chunks) throws IOException;
    
    public Map<ChunkCoord, Chunk> readChunks(Collection<ChunkCoord> chunkCoords);
    
    
    /**
     * Close any opened files.
     */
    public void close();
    
}
