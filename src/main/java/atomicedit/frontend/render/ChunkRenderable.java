
package atomicedit.frontend.render;

import atomicedit.backend.chunk.Chunk;
import atomicedit.backend.chunk.ChunkCoord;
import atomicedit.frontend.ChunkRenderObjectCreator;
import java.util.Collection;

/**
 *
 * @author Justin Bonner
 */
public class ChunkRenderable{
    
    private Collection<ChunkSectionRenderObject> renderObjects;
    private ChunkCoord chunkCoord;
    
    public ChunkRenderable(Chunk chunk, Chunk xMinus, Chunk xPlus, Chunk zMinus, Chunk zPlus){
        this.renderObjects = ChunkRenderObjectCreator.createRenderObjects(chunk, xMinus, xPlus, zMinus, zPlus);
        this.chunkCoord = null;
        this.chunkCoord = chunk.getChunkCoord();
    }
    
    
    public Collection<ChunkSectionRenderObject> getRenderObjects(){
        return this.renderObjects;
    }
    
    public ChunkCoord getChunkCoord(){
        return this.chunkCoord;
    }
    
    @Override
    public String toString(){
        return "ChunkRenderable:" + chunkCoord;
    }
    
}
